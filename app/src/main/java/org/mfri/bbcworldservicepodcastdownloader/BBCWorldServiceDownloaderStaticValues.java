package org.mfri.bbcworldservicepodcastdownloader;

import java.util.HashMap;

public interface BBCWorldServiceDownloaderStaticValues {

    long MILLIS_PER_12H = 12 * 60 * 60 * 1000L;

    String PROGRAM_NEWSHOUR = "newshour";
    String PROGRAM_SPORTSWORLD = "sportsworld";
    String PROGRAM_SPORTSHOUR = "sportshour";
    String PROGRAM_BUSINESSDAILY = "businessdaily";
    String PROGRAM_GLOBALNEWS = "globalnews";

    String PROGRAM_FOOTBALLDAILY = "footballdaily";

    HashMap<String, String> PROGRAM_TITLES_MAP = new HashMap<>() {{
        put("BBC World Service Newshour", PROGRAM_NEWSHOUR);
        put("BBC World Service Sportsworld", PROGRAM_SPORTSWORLD);
        put("BBC World Service Sportshour", PROGRAM_SPORTSHOUR);
        put("BBC World Service Business Daily", PROGRAM_BUSINESSDAILY);
        put("BBC World Service Global News", PROGRAM_GLOBALNEWS);
        put("BBC World Service Football Daily", PROGRAM_FOOTBALLDAILY);
    }};

    HashMap<String, String> INVERSE_PROGRAM_TITLES_MAP = new HashMap<>() {{
        put(PROGRAM_NEWSHOUR, "BBC World Service Newshour");
        put(PROGRAM_SPORTSWORLD, "BBC World Service Sportsworld");
        put(PROGRAM_SPORTSHOUR, "BBC World Service Sportshour");
        put(PROGRAM_BUSINESSDAILY, "BBC World Service Business Daily");
        put(PROGRAM_GLOBALNEWS, "BBC World Service Global News");
        put(PROGRAM_FOOTBALLDAILY, "BBC World Service Football Daily");
    }};

    HashMap<String, String> URL_MAP = new HashMap<>() {{
        put(PROGRAM_NEWSHOUR, "https://www.bbc.co.uk/programmes/p002vsnk/episodes/downloads");
        put(PROGRAM_SPORTSWORLD, "https://www.bbc.co.uk/programmes/p002w5vq/episodes/downloads");
        put(PROGRAM_SPORTSHOUR, "https://www.bbc.co.uk/programmes/p016tmfz/episodes/downloads");
        put(PROGRAM_BUSINESSDAILY, "https://www.bbc.co.uk/programmes/p002vsxs/episodes/downloads");
        put(PROGRAM_GLOBALNEWS, "https://www.bbc.co.uk/programmes/p02nq0gn/episodes/downloads");
        put(PROGRAM_FOOTBALLDAILY, "https://www.bbc.co.uk/programmes/p02nrsln/episodes/downloads");
    }};

    HashMap<String, String> HTTP_STATUS_MAP = new HashMap<String, String>() {{
        put("100", "Continue");
        put("101", "Switching protocols");
        put("102", "Processing");
        put("103", "Early Hints");
        put("200", "OK");
        put("201", "Created");
        put("202", "Accepted");
        put("203", "Non-Authoritative Information");
        put("204", "No Content");
        put("205", "Reset Content");
        put("206", "Partial Content");
        put("207", "Multi-Status");
        put("208", "Already Reported");
        put("226", "IM Used");
        put("300", "Multiple Choices");
        put("301", "Moved Permanently");
        put("302", "Found (Previously \"Moved Temporarily\")");
        put("303", "See Other");
        put("304", "Not Modified");
        put("305", "Use Proxy");
        put("306", "Switch Proxy");
        put("307", "Temporary Redirect");
        put("308", "Permanent Redirect");
        put("400", "Bad Request");
        put("401", "Unauthorized");
        put("402", "Payment Required");
        put("403", "Forbidden");
        put("404", "Not Found");
        put("405", "Method Not Allowed");
        put("406", "Not Acceptable");
        put("407", "Proxy Authentication Required");
        put("408", "Request Timeout");
        put("409", "Conflict");
        put("410", "Gone");
        put("411", "Length Required");
        put("412", "Precondition Failed");
        put("413", "Payload Too Large");
        put("414", "URI Too Long");
        put("415", "Unsupported Media Type");
        put("416", "Range Not Satisfiable");
        put("417", "Expectation Failed");
        put("418", "I'm a Teapot");
        put("421", "Misdirected Request");
        put("422", "Unprocessable Entity");
        put("423", "Locked");
        put("424", "Failed Dependency");
        put("425", "Too Early");
        put("426", "Upgrade Required");
        put("428", "Precondition Required");
        put("429", "Too Many Requests");
        put("431", "Request Header Fields Too Large");
        put("451", "Unavailable For Legal Reasons");
        put("500", "Internal Server Error");
        put("501", "Not Implemented");
        put("502", "Bad Gateway");
        put("503", "Service Unavailable");
        put("504", "Gateway Timeout");
        put("505", "HTTP Version Not Supported");
        put("506", "Variant Also Negotiates");
        put("507", "Insufficient Storage");
        put("508", "Loop Detected");
        put("510", "Not Extended");
        put("511", "Network Authentication Required");
    }};

}
