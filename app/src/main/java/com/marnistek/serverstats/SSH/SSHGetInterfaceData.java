package com.marnistek.serverstats.SSH;

import android.os.Environment;
import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class SSHGetInterfaceData {

    public static void executeRemoteCommand(
            String username,
            String password,
            String hostname,
            int port,
            String command,
            int timeoutSecs) throws Exception {

        JSch jsch = new JSch();

        int timeout = timeoutSecs * 1000;

        String knownHostsFile = "known_hosts.txt";
        String knownHostsPath = Environment.getExternalStorageDirectory() + "/ServerStats/" + knownHostsFile;

        jsch.setKnownHosts(knownHostsPath);

        HostKeyRepository hkr=jsch.getHostKeyRepository();
        HostKey[] hks=hkr.getHostKey();

        for (HostKey hk : hks) {
            Log.d("HOSTKEY CHECK",hk.getHost() + " " +
                    hk.getType() + " " +
                    hk.getFingerPrint(jsch));
        }

        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        String result = "TEST!";

        // Avoid asking for key confirmation
        //Properties prop = new Properties();
        //prop.put("StrictHostKeyChecking", "no");
        //session.setConfig(prop);
        session.setConfig("PreferredAuthentications","password");

        session.connect(timeout);

        // SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        // Execute command
        channelssh.setCommand(command);
        channelssh.connect();

        InputStream in = channelssh.getInputStream();
        InputStream err = channelssh.getErrStream();
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(in));
        BufferedReader bufferedReaderErr = new BufferedReader(new InputStreamReader(err));

        File direct = new File(Environment.getExternalStorageDirectory()
                + "/ServerStats/");
        if (!direct.exists()) {
            direct.mkdirs();
        }
        String fileName = "interfaces.txt";
        String path = Environment.getExternalStorageDirectory() + "/ServerStats/" + fileName;
        File targetFile = new File(path);
        FileOutputStream fos = new FileOutputStream(targetFile);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        String line = bufferedReader.readLine();

        while (line != null){
            bw.write(line);
            bw.newLine();
            line = bufferedReader.readLine();
        }

        bw.close();

        if(bufferedReaderErr.readLine() != null) {
            Log.d("INTERFACE SSH ERROR", bufferedReaderErr.readLine());
        }
        bufferedReaderErr.close();


        channelssh.disconnect();
        session.disconnect();

        //channelssh.disconnect();

        //return result;
    }
}
