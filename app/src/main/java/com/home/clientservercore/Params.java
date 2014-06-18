package com.home.clientservercore;


public class Params {

    private static final String NAMESPACE = Params.class.getName();

    public static class Intent {

        private Intent() {
            // hidden
        }

        public final static String EXTRA_COMMAND = NAMESPACE.concat(".EXTRA_COMMAND");
        public final static String EXTRA_RECEIVER = NAMESPACE.concat(".EXTRA_RECEIVER");
    }

}
