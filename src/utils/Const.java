package utils;


public class Const {
    public static class REQUEST_TYPE{
        public static final String READ = "read";
        public static final String WRITE = "write";
        public static final String REGISTER = "register";
        public static final String CALLBACK = "callback";
        public static final String APPEND = "append";
        public static final String LAST_UPDATE = "get_last_update";
        public static final String READ_ALL_FILE = "read_all";
        public static final String WRITE_ALL_FILE = "write_all";
    }
    public static class MESSAGE{
        public static final String REGISTER_SUCCESS = "You have registered successfully!";
        public static final String REGISTER_EXPIRE = "Your monitor interval has expired";
        public static final String ERROR = "Error";
    }

    public static class SEMANTIC{
        public static final String AT_LEAST_1 = "at_least_1";
        public static final String AT_MOST_1 =  "at_most_1";
    }
}
