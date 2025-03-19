package com.modern.chating.twilio;

public class TwilioInstance {
    private static TwilioExecute instance;

    public static void init(TwilioExecute twilioExecute) {
        instance = twilioExecute;
    }

    public static TwilioExecute getInstance() {
        return instance;
    }
}
