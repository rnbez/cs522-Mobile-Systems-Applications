package edu.stevens.cs522.bookstore.contracts;

/**
 * Created by Rafael on 2/17/2016.
 */
public final class TransitContract {
    public static final String AUTHORITY = "com.example.TransitProvider";
    public static final String SCHEME = "content://";
    public static final String SLASH = "/";
    public static final String DATABASE_NAME = "transit.db";

    /* An array list of all the SQL create table statements */
    public static final String[] SQL_CREATE_TABLE_ARRAY = {
            Agency.CREATE_TABLE,
            SLASH.CREATE_TABLE,
            Calendar.CREATE_TABLE,
            Routes.CREATE_TABLE,
            Shapes.CREATE_TABLE,
            Stops.CREATE_TABLE,
            StopTimes.CREATE_TABLE,
            Trips.CREATE_TABLE
    };

    /**
     * Array of resource ids for each GTFS data file that will be loaded into
     * database
     */
    public static final int[] RAW_IDS = {
            R.raw.agency,
            R.raw.calendar_dates,
            R.raw.calendar,
            R.raw.routes,
            R.raw.shapes,
            R.raw.stops,
            R.raw.stop_times,
            R.raw.trips,
    };

    /* Do not allow this class to be instantiated */
    private TransitContract() {
    }

    public static final class Agency implements BaseColumns {
        /* Do not allow this class to be instantiated */
        private Agency() {
        }

        public static final String TABLE_NAME = "Agency";

        public static final String KEY_AGENCY_ID = "AgencyId";

        public static final String KEY_NAME = "Name";

        public static final String KEY_URL = "Url";

        public static final String KEY_TIMEZONE = "Timezone";

        public static final String KEY_LANG = "Language";

        public static final String KEY_PHONE = "PhoneNumber";

        public static final String KEY_FARE_URL = "FareUrl";

        /*
         * URI definitions
         */

        /**
         * The content style URI
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME);

        /**
         * The content URI base for a single row. An ID must be appended.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + SLASH + TABLE_NAME + SLASH);

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = KEY_AGENCY_ID + " ASC";

        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #CONTENT_URI} providing rows
         */
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE +
                "/vnd.com.marylandtransitcommuters.agency";

        /**
         * The MIME type of a {@link #CONTENT_URI} single row
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/vnd.com.marylandtransitcommuters.agency";

        /**
         * SQL Statement to create the routes table
         */
        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + _ID + " INTEGER PRIMARY KEY,"
                + KEY_AGENCY_ID + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_URL + " TEXT,"
                + KEY_TIMEZONE + " TEXT,"
                + KEY_LANG + " TEXT,"
                + KEY_PHONE + " TEXT,"
                + KEY_FARE_URL + " TEXT"
                + ");";

        /**
         * SQL statement to delete the table
         */
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * Array of all the columns. Makes for cleaner code
         */
        public static final String[] KEY_ARRAY = {
                KEY_AGENCY_ID,
                KEY_NAME,
                KEY_URL,
                KEY_TIMEZONE,
                KEY_LANG,
                KEY_PHONE,
                KEY_FARE_URL
        };
    }

}