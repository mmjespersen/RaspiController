package mmj.raspicontroller;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Dns;

/**
 * OkHttp DNS that falls back to multicast DNS (mDNS) on the local network.
 *
 * Android only resolves names through the configured DNS server, so a bare LAN name like
 * "robotpi" fails (Windows finds it through LLMNR/mDNS, Android does not). The Raspberry Pi
 * announces itself as "robotpi.local" over mDNS (avahi), so if normal DNS fails we ask the
 * local network for "<name>.local" ourselves.
 */
public class MdnsDns implements Dns {

    private static final String TAG = "MdnsDns";
    private static final int MDNS_PORT = 5353;
    private static final String MDNS_GROUP = "224.0.0.251";
    private static final int TIMEOUT_MS = 1500;
    private static final int TRIES = 2;

    @NonNull
    @Override
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
        String host = hostname.toLowerCase(Locale.ROOT);
        UnknownHostException systemError = null;

        // Normal DNS first (also handles plain IP addresses), except for .local which is mDNS only
        if (!host.endsWith(".local")) {
            try {
                return Dns.SYSTEM.lookup(hostname);
            } catch (UnknownHostException e) {
                systemError = e;
            }
        }

        // Only try mDNS for LAN-style names: "robotpi" or "robotpi.local"
        if (host.endsWith(".local") || !host.contains(".")) {
            String mdnsName = host.endsWith(".local") ? host : host + ".local";
            InetAddress address = queryMdns(mdnsName);
            if (address != null) {
                Log.i(TAG, hostname + " resolved via mDNS (" + mdnsName + ") to " + address.getHostAddress());
                List<InetAddress> result = new ArrayList<>();
                result.add(address);
                return result;
            }
        }

        if (systemError != null) throw systemError;
        throw new UnknownHostException("Unable to resolve host " + hostname + " via mDNS");
    }

    /** Sends an mDNS A-record query and returns the first IPv4 answer, or null. */
    private static InetAddress queryMdns(String name) {
        byte[] query = buildQuery(name);
        // Using an ephemeral port (not 5353) makes this a "legacy unicast" query:
        // the responder answers straight back to our socket, no multicast receive needed.
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            InetAddress group = InetAddress.getByName(MDNS_GROUP);
            byte[] buf = new byte[1500];
            for (int attempt = 0; attempt < TRIES; attempt++) {
                socket.send(new DatagramPacket(query, query.length, group, MDNS_PORT));
                long deadline = System.currentTimeMillis() + TIMEOUT_MS;
                while (System.currentTimeMillis() < deadline) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    try {
                        socket.receive(packet);
                    } catch (SocketTimeoutException e) {
                        break;
                    }
                    InetAddress address = parseARecord(packet.getData(), packet.getLength(), name);
                    if (address != null) return address;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "mDNS query for " + name + " failed", e);
        }
        return null;
    }

    private static byte[] buildQuery(String name) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Header: id, flags, qdcount=1, ancount, nscount, arcount
        int id = (int) (Math.random() * 0xFFFF);
        out.write(id >> 8); out.write(id & 0xFF);
        out.write(0); out.write(0);
        out.write(0); out.write(1);
        out.write(0); out.write(0);
        out.write(0); out.write(0);
        out.write(0); out.write(0);
        for (String label : name.split("\\.")) {
            byte[] b = label.getBytes(StandardCharsets.US_ASCII);
            out.write(b.length);
            out.write(b, 0, b.length);
        }
        out.write(0);
        out.write(0); out.write(1);        // QTYPE A
        out.write(0x80); out.write(1);     // QCLASS IN, with "unicast response" bit
        return out.toByteArray();
    }

    /** Parses a DNS response and returns the IPv4 address of an A record for {@code name}. */
    private static InetAddress parseARecord(byte[] data, int length, String name) {
        try {
            if (length < 12) return null;
            int qd = u16(data, 4), an = u16(data, 6), ns = u16(data, 8), ar = u16(data, 10);
            int[] pos = {12};
            for (int i = 0; i < qd; i++) {
                readName(data, length, pos);
                pos[0] += 4;
            }
            int records = an + ns + ar;
            for (int i = 0; i < records; i++) {
                String rrName = readName(data, length, pos);
                if (pos[0] + 10 > length) return null;
                int type = u16(data, pos[0]);
                int rdLength = u16(data, pos[0] + 8);
                int rdStart = pos[0] + 10;
                if (rdStart + rdLength > length) return null;
                if (type == 1 && rdLength == 4 && rrName.equalsIgnoreCase(name)) {
                    byte[] ip = {data[rdStart], data[rdStart + 1], data[rdStart + 2], data[rdStart + 3]};
                    return InetAddress.getByAddress(name, ip);
                }
                pos[0] = rdStart + rdLength;
            }
        } catch (Exception e) {
            Log.d(TAG, "Ignoring malformed mDNS packet", e);
        }
        return null;
    }

    /** Reads a (possibly compressed) DNS name at pos[0] and advances pos[0] past it. */
    private static String readName(byte[] data, int length, int[] pos) {
        StringBuilder sb = new StringBuilder();
        int p = pos[0];
        boolean jumped = false;
        int jumps = 0;
        while (true) {
            if (p >= length) throw new IllegalArgumentException("name out of bounds");
            int len = data[p] & 0xFF;
            if (len == 0) {
                if (!jumped) pos[0] = p + 1;
                break;
            }
            if ((len & 0xC0) == 0xC0) {
                if (p + 1 >= length || ++jumps > 20) throw new IllegalArgumentException("bad pointer");
                if (!jumped) pos[0] = p + 2;
                p = ((len & 0x3F) << 8) | (data[p + 1] & 0xFF);
                jumped = true;
                continue;
            }
            if (p + 1 + len > length) throw new IllegalArgumentException("label out of bounds");
            if (sb.length() > 0) sb.append('.');
            sb.append(new String(data, p + 1, len, StandardCharsets.US_ASCII));
            p += 1 + len;
        }
        return sb.toString();
    }

    private static int u16(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }
}
