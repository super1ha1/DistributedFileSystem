package server;


public class Const {
    public static class REQUEST_TYPE{
        public static final String READ = "read";
        public static final String WRITE = "write";
        public static final String REGISTER = "register";
        public static final String CALLBACK = "callback";
        public static final String APPEND = "append";
        public static final String LAST_UPDATE = "get_last_update";
        public static final String READ_ALL_FILE = "read_all";
    }
    public static class MESSAGE{
        public static final String REGISTER_SUCCESS = "You have registered successfully!";
        public static final String REGISTER_EXPIRE = "Your monitor interval has expired";
    }
}
