package com.hijacker;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.hijacker.MainActivity.debug;
import static com.hijacker.MainActivity.load;
import static com.hijacker.MainActivity.path;
import static com.hijacker.MainActivity.pref_edit;
import static com.hijacker.MainActivity.shell;
import static com.hijacker.MainActivity.shell3_in;
import static com.hijacker.MainActivity.su_thread;

public class InstallToolsDialog extends DialogFragment {
    View view;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.install_tools, null);

        builder.setView(view);
        builder.setTitle(R.string.install_tools_title);
        builder.setMessage(R.string.install_message);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //close
            }
        });
        builder.setPositiveButton("Install", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        return builder.create();
    }
    @Override
    public void onStart() {
        super.onStart();
        //Override positiveButton action to dismiss the fragment only when the directories exist, not on error
        AlertDialog d = (AlertDialog)getDialog();
        if(d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(shell==null){
                        su_thread.start();
                        try{
                            //Wait for su shells to spawn
                            su_thread.join();
                        }catch(InterruptedException ignored){}
                    }
                    String tools_location = ((EditText)view.findViewById(R.id.tools_location)).getText().toString();
                    String lib_location = ((EditText)view.findViewById(R.id.lib_location)).getText().toString();
                    File tools = new File(tools_location);
                    File lib = new File(lib_location);
                    if(!tools.exists()){
                        Toast.makeText(getActivity().getApplicationContext(), "Directory for tools doesn't exist", Toast.LENGTH_SHORT).show();
                    }else if(!lib.exists()){
                        Toast.makeText(getActivity().getApplicationContext(), "Directory for library doesn't exist", Toast.LENGTH_SHORT).show();
                    }else{
                        if(debug){
                            Log.d("InstallToolsDialog", "Installing Tools in " + tools_location);
                            Log.d("InstallToolsDialog", "Installing Library in " + lib_location);
                        }
                        shell3_in.print("busybox mount -o rw,remount,rw /system\n");
                        shell3_in.flush();
                        shell3_in.print("cd " + path + "\nrm !(oui.txt)\n");
                        shell3_in.flush();
                        extract("airbase-ng", tools_location);
                        extract("aircrack-ng", tools_location);
                        extract("aireplay-ng", tools_location);
                        extract("airodump-ng", tools_location);
                        extract("besside-ng", tools_location);
                        extract("ivstools", tools_location);
                        extract("iw", tools_location);
                        extract("iwconfig", tools_location);
                        extract("iwlist", tools_location);
                        extract("iwpriv", tools_location);
                        extract("kstats", tools_location);
                        extract("makeivs-ng", tools_location);
                        extract("mdk3", tools_location);
                        extract("nc", tools_location);
                        extract("packetforge-ng", tools_location);
                        extract("wesside-ng", tools_location);
                        extract("wpaclean", tools_location);
                        extract("libfakeioctl.so", lib_location);
                        shell3_in.print("busybox mount -o ro,remount,ro /system\n");
                        shell3_in.flush();
                        Toast.makeText(getActivity().getApplicationContext(), "Installed tools and lib", Toast.LENGTH_LONG).show();
                        pref_edit.putString("prefix", "LD_PRELOAD=" + lib_location + "/libfakeioctl.so");
                        pref_edit.commit();
                        load();
                        Toast.makeText(getActivity().getApplicationContext(), "Command Prefix adjusted for library path", Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                }
            });
        }
    }
    void extract(String filename, String dest){
        File f = new File(path, filename);      //no permissions to write at dest so extract at local directory and then move to target
        dest = dest + '/' + filename;
        if(!f.exists()){
            try{
                InputStream in = getResources().getAssets().open(filename);
                FileOutputStream out = new FileOutputStream(f);
                byte[] buf = new byte[1024];
                int len;
                while((len = in.read(buf))>0){
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                shell3_in.print("mv " + path + '/' + filename + " " + dest + '\n');
                shell3_in.print("chmod 755 " + dest + '\n');
                shell3_in.flush();
            }catch(IOException e){
                Log.e("FileProvider", "Exception copying from assets", e);
            }
        }
    }
}
