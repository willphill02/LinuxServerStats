package com.marnistek.serverstats.SSH;


import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.Properties;

public class SSHGetFingerprint {

    public static String executeRemoteCommand(
            String username,
            String password,
            String hostname,
            int port,
            int timeoutSecs) throws Exception {

        JSch jsch = new JSch();

        int timeout = timeoutSecs * 1000;

        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);
        session.setConfig("PreferredAuthentications",
                "password");

        session.connect(timeout);

        HostKey hk = session.getHostKey();
        String fingerprint = hk.getFingerPrint(jsch);
        Log.d("FINGERPRINT",fingerprint);


        // SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        // Execute command
        channelssh.setCommand("date");
        channelssh.connect();


        channelssh.disconnect();
        session.disconnect();

        return fingerprint;
    }
}