package com.example.raspicontroller;

import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.raspicontroller.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

import mmj.raspicontroller.RaspiRobot;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showRobotUrlDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Lets the user change the robot's drive WebSocket URL; saved on the device. */
    private void showRobotUrlDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setSingleLine(true);
        input.setText(RobotSettings.getDriveUrl(this));
        input.setSelection(input.getText().length());
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(this);
        container.setPadding(pad, pad / 2, pad, 0);
        container.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Robot URL")
                .setMessage("Drive WebSocket, e.g. " + RobotSettings.DEFAULT_DRIVE_URL)
                .setView(container)
                .setPositiveButton("Save", null)   // set below so an invalid URL keeps the dialog open
                .setNeutralButton("Default", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                input.setText(RobotSettings.DEFAULT_DRIVE_URL);
                input.setSelection(input.getText().length());
            });
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String url = input.getText().toString().trim();
                if (!RaspiRobot.isValidUrl(url)) {
                    input.setError("Must be a ws:// or wss:// URL, e.g. " + RobotSettings.DEFAULT_DRIVE_URL);
                    return;
                }
                RobotSettings.setDriveUrl(this, url);
                FirstFragment first = findFirstFragment();
                if (first != null) first.applyRobotUrl(url);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private FirstFragment findFirstFragment() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment == null) return null;
        for (Fragment f : navHostFragment.getChildFragmentManager().getFragments()) {
            if (f instanceof FirstFragment) return (FirstFragment) f;
        }
        return null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment != null) {
            List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();

            if (!fragments.isEmpty()) {
                Fragment currentFragment = fragments.get(0);

                if (currentFragment instanceof FirstFragment) {
                    ((FirstFragment) currentFragment).onGenericMotionEvent(event);
                }
            }
        }


        //GetAllAxis(event);
        //return controllerManager.handleMotionEvent(event) || super.onGenericMotionEvent(event);
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment != null) {
            List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();

            if (!fragments.isEmpty()) {
                Fragment currentFragment = fragments.get(0);

                if (currentFragment instanceof FirstFragment) {
                    ((FirstFragment) currentFragment).onKeyDown(keyCode, event);
                }
            }
        }


        return super.onKeyDown(keyCode, event);
//        return controllerManager.handleKeyEvent(event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment != null) {
            List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();

            if (!fragments.isEmpty()) {
                Fragment currentFragment = fragments.get(0);

                if (currentFragment instanceof FirstFragment) {
                    ((FirstFragment) currentFragment).onKeyUp(keyCode, event);
                }
            }
        }
        return super.onKeyUp(keyCode, event);
//        return controllerManager.handleKeyEvent(event) || super.onKeyUp(keyCode, event);
    }

}

