package com.github.kiu345.eclipse.eclipseai.adapter;

public interface ResponseListener {
    public class Event {
        private String thinking;
        private String message;
        private Throwable error;

        protected Event() {
        }

        public static Event errorResponse(Throwable error) {
            Event result = new Event();
            result.error = error;
            return result;
        }

        public static Event messageResponse(String message) {
            Event result = new Event();
            result.message = message;
            return result;
        }

        public static Event thinkingResponse(String thinking) {
            Event result = new Event();
            result.thinking = thinking;
            return result;
        }

        public static Event response(String message, String thinking) {
            Event result = new Event();
            result.message = message;
            result.thinking = thinking;
            return result;
        }

        public String thinking() {
            return thinking;
        }

        public String message() {
            return message;
        }

        public Throwable error() {
            return error;
        }
    }

    default void onPartialThinkResponse(String message) {
    }

    default void onPartialResponse(String message) {
    }

    public void onResponse(Event response);

    default void onError(Throwable e) {
        e.printStackTrace();
    }
}
