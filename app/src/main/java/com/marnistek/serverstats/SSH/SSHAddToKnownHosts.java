package com.marnistek.serverstats.SSH;


import android.os.Environment;
import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class SSHAddToKnownHosts {

    public static String executeRemoteCommand(
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

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);
        session.setConfig("PreferredAuthentications",
                "password");

        session.connect(timeout);


        HostKey hk = session.getHostKey();
        Log.d("HOSTKEY CHECK","HostKey: "+
                hk.getHost()+" "+
                hk.getType()+" "+
                hk.getFingerPrint(jsch));


        // SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        // Execute command
        channelssh.setCommand(command);
        channelssh.connect();

        InputStream in = channelssh.getInputStream();
        InputStream err = channelssh.getErrStream();

        StringBuilder content = new StringBuilder();
        StringBuilder error = new StringBuilder();

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(in));

        BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(err));

        String line = bufferedReader.readLine();
        String errLine = errorReader.readLine();

        while (line != null){
            content.append(line);
            line = bufferedReader.readLine();
        }

        while (errLine != null){
            error.append(errLine);
            errLine = errorReader.readLine();
        }

        //Log.d("JSCHERROR",errLine);

        bufferedReader.close();
        errorReader.close();

        channelssh.disconnect();
        session.disconnect();

        return content.toString();
    }
}