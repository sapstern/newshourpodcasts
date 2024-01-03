package org.mfri.bbc.mediamanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public final class BBCWorldServiceDownloaderUtils implements BBCWorldServiceDownloaderStaticValues {

    private HashMap<String, Bundle> currentDownloadOptionsMap = null;
    private HashMap<String, Date> timeStampOfcurrentDownloadOptionsMap = null;

    private HashMap<String, PeriodicWorkRequest> downLoadRequestMap = null;

    private static Constraints DOWNLOADCONSTRAINTS = null;

    private static final BBCWorldServiceDownloaderUtils INSTANCE = new BBCWorldServiceDownloaderUtils();


    private BBCWorldServiceDownloaderUtils(){
        downLoadRequestMap = new HashMap<String, PeriodicWorkRequest>();
        currentDownloadOptionsMap = new HashMap<String, Bundle>();
        timeStampOfcurrentDownloadOptionsMap = new HashMap<String, Date>();
    }

    public static BBCWorldServiceDownloaderUtils getInstance()
    {
        return INSTANCE;
    }

    public PeriodicWorkRequest getDownLoadRequest(String theProgram){

        PeriodicWorkRequest downLoadRequest = downLoadRequestMap.get(theProgram);
        if(downLoadRequest == null){
            downLoadRequest = new PeriodicWorkRequest.Builder(DownloadWorker.class, 1, TimeUnit.HOURS)
                    .setInputData(new Data.Builder().putString("PROGRAM_TYPE", theProgram).build())
                    .setConstraints(getDownLoadConstraints())
                    .build();

        }
        return downLoadRequest;
    }

    private static Constraints getDownLoadConstraints(){
        if (DOWNLOADCONSTRAINTS==null) {
            DOWNLOADCONSTRAINTS =  new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build();
        }
        return DOWNLOADCONSTRAINTS;
    }

    /*
     * Checks the network state for connection
     *
     * */
    public static boolean isDeviceConnected(Context theContext) {
        ConnectivityManager cm = (ConnectivityManager) theContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public boolean isWlanConnection(Context context){
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            return true;
        }
        return false;
    }

    public synchronized Bundle getDownloadedPodcastsBundle(Context context, String theProgram) throws IOException{
        Log.d("Utils", "getDownloadedPodcasts() start" );
        Bundle bundle = new Bundle();
        ArrayList<DownloadItem> tab = getDownloadedPodcastsList(context, theProgram);
        if (tab == null) return null;
        //Convert TempDLItem to DownloadListItem
        for(int i=0;i<tab.size();i++)
        {
            DownloadItem currentItem = tab.get(i);
            DownloadListItem item = new DownloadListItem(String.valueOf(i),currentItem.content,currentItem.url,currentItem.dateOfPublication,currentItem.fileName);
            bundle.putParcelable("ITEM_" +i, item);
        }
        Log.d("Utils", "getDownloadedPodcasts() end size of list: "+tab.size() );
        bundle.putInt("LIST_SIZE", tab.size());
        return bundle;
    }

    @Nullable
    public ArrayList<DownloadItem> getDownloadedPodcastsList(Context context, String theProgram) {
        ArrayList<DownloadItem> tab = new ArrayList<>();
        String root = PreferenceManager.getDefaultSharedPreferences(context).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());
        File myDir = new File(root+"/"+theProgram);
        File[] podcastArry = myDir.listFiles();
        if(podcastArry==null)
            return null;


        String startOfFilename = StringUtils.capitalize(theProgram);

        for (File file : podcastArry) {
            if (file != null
                    && !file.isDirectory()
                    && file.getName().startsWith(startOfFilename+"_")
                    && file.getName().endsWith(".mp3")
            ) {

                String theFileName = file.getName();
                StringTokenizer toki = new StringTokenizer(file.getName().substring(0, file.getName().indexOf(".mp3")), "_");
                StringBuilder theDescriptionBuilder = new StringBuilder();
                StringBuilder theDateBuilder = new StringBuilder();
                boolean isDate = false;
                while (toki.hasMoreElements()) {
                    String currentToken = toki.nextToken();
                    if (currentToken.equals("Mon")
                            || currentToken.equals("Tue")
                            || currentToken.equals("Wed")
                            || currentToken.equals("Thu")
                            || currentToken.equals("Fri")
                            || currentToken.equals("Sat")
                            || currentToken.equals("Sun")
                    ) {
                        isDate = true;
                    }
                    if (isDate) {
                        theDateBuilder.append(currentToken);
                        if (toki.hasMoreElements())
                            theDateBuilder.append("_");
                    } else {
                        if (!currentToken.startsWith(startOfFilename))
                            theDescriptionBuilder.append(currentToken);
                        if (toki.hasMoreElements())
                            theDescriptionBuilder.append(" ");
                    }
                }

                String dateString = theDateBuilder.toString();
                Log.d("Utils", "getDownloadedPodcastsList got the following date for comparison: "+dateString);
                DownloadItem localItem = new DownloadItem("X", theDescriptionBuilder.toString(), "none", dateString, theFileName);
                localItem.compareDate = getDateFromPatternString(dateString, "E_d_MMMM_yyyy_HH_mm");
                tab.add(localItem);
            }
        }
        //Sort by date descending
        Collections.sort(tab);
        return tab;
    }

    /**
     * @param dateString
     * @param datePattern
     * @return Date Object generated
     */
    public Date getDateFromPatternString(String dateString, String datePattern){

        DateFormat df = new SimpleDateFormat(datePattern, Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return getParsedDate(dateString, df);
    }

    private Date getParsedDate(String patternString, DateFormat df) {
        try {
            return df.parse(patternString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(1220227200);
        }
    }

    /**
     *
     * loads dl options from BBC
     * @param context
     * @return bundle hplding download options
     * @throws IOException
     */
    public synchronized Bundle getCurrentDownloadOptions(Context context, String theProgram)  {

        Bundle currentDownloadOptions = currentDownloadOptionsMap.get(theProgram);
        Date timeStampOfcurrentDownloadOptions = timeStampOfcurrentDownloadOptionsMap.get(theProgram);


        if(currentDownloadOptions !=null && isWithinTimeFrame(timeStampOfcurrentDownloadOptions)) {
            return currentDownloadOptions;
        }
        if(!isDeviceConnected(context))
            return null;
        currentDownloadOptions = new Bundle();

        //MFRI jsoup rein
        String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.87 Safari/537.36";
        Document doc = null;
        try {
            doc = Jsoup.connect(URL_MAP.get(theProgram)).userAgent(userAgent).get();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        List<DownloadItem> theDownloadItemsFromJson = null;
        Elements theJsonElements = doc.select("script[type]");
        for(int i=0;i<theJsonElements.size();i++){
            if(        theJsonElements.get(i).attr("type") != null
                    && theJsonElements.get(i).attr("type").equals("application/ld+json")
                    && theJsonElements.get(i).data().trim().startsWith("{\"@type\":\"RadioSeries\"")){
                System.out.println(theJsonElements.get(i));
                try {
                    theDownloadItemsFromJson = parseJson(theJsonElements.get(i).data());
                    if(theDownloadItemsFromJson!=null&&theJsonElements.size()>0){
                        break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        ArrayList<DownloadItem> dlItemList = new ArrayList<>();
        Elements theElements = doc.select("a[href]");
        String publicationDate;
        //First check for the Element items which are eligible
        List<Element> theElementList  = extractElementList(theElements, context.getResources().getStringArray(R.array.dl_qual_arry),  PreferenceManager.getDefaultSharedPreferences(context));
        Iterator<Element> theIteratorElements = theElementList.iterator();
        int s = 0;
        while(theIteratorElements.hasNext()) {
            Element currentElement = theIteratorElements.next();
            if (currentElement.attr("title") != null && currentElement.attr("title").indexOf("days left to listen") != -1) {
                String theTitle = currentElement.attr("title");
                int startIndexOfText = theTitle.indexOf("days left to listen");
                int dateOffset = Integer.parseInt((theTitle.substring(0, startIndexOfText)).trim());
                Log.d("TITLE", theTitle);
                int startIndex = theTitle.indexOf("(");
                int endIndex = theTitle.indexOf(")");
                if (startIndex != -1 && endIndex != -1) {

                    startIndex = startIndex + 1;
                    publicationDate = theTitle.substring(startIndex, endIndex);
                    //publicationDate = (publicationDate + "_" + theTitle.substring(startIndex, endIndex)).trim();
                    //Thu 24 September 2020, 15:00
                    publicationDate = getPublicationDate(publicationDate, dateOffset);
                    Log.d("PUB_DATE", publicationDate);
                }
            }
            Log.d("ELEMENT", currentElement.text());


            Log.d("ATTRIBUT_TEXT", currentElement.attr("download"));
            Log.d("ATTRIBUT_HREF", currentElement.attr("href"));
            String[] tokens = currentElement.attr("download").split("-");
            String theDescription = "";
            String theId = "";
            for (int j = 0; j < tokens.length; j++) {
                switch (j) {
                    case 0:
                        theDescription = tokens[j];
                        break;
                    case 1:
                        theId = tokens[j];
                        theId = theId.replace(".mp3", "").trim();
                        break;
                }
            }

            publicationDate = parsePublicationDate(theId, theDownloadItemsFromJson);
            String theFilename = prepareFilename(theDescription, publicationDate, theProgram);
            if (publicationDate.equals("Mon 01 Januar 0000, 00:00")) {
                continue;
            }

            String dlURL = "https:" + currentElement.attr("href");
            DownloadItem item = new DownloadItem(String.valueOf(s), theDescription, dlURL, publicationDate, theFilename);
            //MFRI comparedate
            item.compareDate = getDateFromPatternString(publicationDate, "E d MMMM yyyy, HH:mm");

            if(!isInList(dlItemList.iterator(), item)) {
                dlItemList.add(item);
                s++;
            }
        }
        Log.d("onHandleIntent size: ", String.valueOf(s));
        //Sort by date descending


        Collections.sort(dlItemList);
        for (int z=0; z<dlItemList.size();z++){
            DownloadItem dlitem = dlItemList.get(z);
            DownloadListItem downloadListItem = new DownloadListItem(dlitem.id, dlitem.content, dlitem.url, dlitem.dateOfPublication, dlitem.fileName);
            currentDownloadOptions.putParcelable("ITEM_" + z, downloadListItem);
        }
        currentDownloadOptions.putInt("LIST_SIZE", s);
        currentDownloadOptionsMap.put(theProgram,currentDownloadOptions);
        Log.d("HANDLE", "handleActionDownloadList exit");
        return currentDownloadOptions;
    }
    private boolean isInList(Iterator<DownloadItem> iter, DownloadItem item){

        while (iter.hasNext()){
            DownloadItem currentItem = iter.next();
            if (currentItem.content.equals(item.content))
                return true;
        }
        return false;

    }
    /*
     * First look if we get anything for the selected dl quality, if not test if we get something for the
     * unselected dl quality (very clumsy, but due to bbc inconsistent maintenace of their dl pages)
     */
    private List<Element> extractElementList(Elements theElements, String[] arryQualities, SharedPreferences prefs) {

        List<Element>  theElementList = new LinkedList<>();
        String selectedQuality = prefs.getString("dl_qual","Lower quality (64kbps)");
        String unselectedQuality = null;


        for(int i=0;i<arryQualities.length;i++){
            if(!arryQualities[i].equals(selectedQuality)){
                unselectedQuality = arryQualities[i];
                break;
            }
        }
        for (int i = 0; i < theElements.size(); i++) {
            Element element =  theElements.get(i);
            if(isInQuality(element,selectedQuality)){
                theElementList.add(element);
                continue;
            }
            if(isInQuality(element,unselectedQuality)){
                theElementList.add(element);
            }
        }
        return theElementList;
    }

    private boolean isInQuality(Element currentElement, String theQuality) {

        if (currentElement.text().startsWith(theQuality)) {
            return true;
        }
        return false;
    }

    private String parsePublicationDate(String theId, List<DownloadItem> theDownloadItemsFromJson) {

        Iterator<DownloadItem> iterator = theDownloadItemsFromJson.iterator();
        while (iterator.hasNext()){
            DownloadItem item = iterator.next();
            if(item.id.equals(theId)){
                return processDateFromJson(item.dateOfPublication);
            }
        }
        return "Mon 01 Januar 0000, 00:00";
    }

    private static String processDateFromJson(String dateInString) {

        //
        DateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        DateFormat formatOut = new SimpleDateFormat("E d MMMM yyyy, HH:mm", Locale.ENGLISH);
        try {
            Date date = formatIn.parse(dateInString);
            //date = getDate(date, 30, "add");
            return formatOut.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Mon 01 Januar 0000, 00:00";
        }
    }

    private List<DownloadItem> parseJson(String jsonString) throws JSONException {
        List<DownloadItem> resultList = new LinkedList<DownloadItem>();
        JSONObject jsonObj = new JSONObject(jsonString);
        // Getting JSON Array node
        JSONArray episodes = jsonObj.getJSONArray("hasPart");
        for (int i=0;i<episodes.length();i++){
            DownloadItem currentItem = new DownloadItem();
            JSONObject currentEpisode = episodes.getJSONObject(i);
            currentItem.id = currentEpisode.getString("identifier");
            currentItem.content = currentEpisode.getString("description");
            JSONObject publication = currentEpisode.getJSONObject("publication");
            currentItem.dateOfPublication = publication.getString("startDate");
            resultList.add(currentItem);
        }
        return resultList;
    }
    /**
     * checks whether we are now less than 12 hours after the last run
     * If not, returns false
     *
     * @param timeStampOfcurrentDownloadOptions
     * @return
     */
    private boolean isWithinTimeFrame(Date timeStampOfcurrentDownloadOptions) {

        if(timeStampOfcurrentDownloadOptions==null)
            return false;
        Date currentDate = new Date();
        //first check simply whether last timstamp is more than 12 hours
        if (Math.abs(timeStampOfcurrentDownloadOptions.getTime() - currentDate.getTime()) > MILLIS_PER_12H)
            return false;

        //Get a GMT timezone calendar
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));

        cal.setTime(currentDate);
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        //Check whether last relevant publication date was at 15 00 GMT or 22 00 GMT
        boolean isBetween15And22 = currentHour > 15 && currentHour < 22;
        if(isBetween15And22){
            cal.set(Calendar.HOUR, 15);
        }
        else{
            cal.set(Calendar.HOUR, 22);
        }
        cal.set(Calendar.MINUTE, 00);
        //Now compare the timestamp of the last run was made after the last publication date
        //Then we do not need to fetch the currently available downloads again
        return timeStampOfcurrentDownloadOptions.getTime() > cal.getTime().getTime();

    }

    /**
     * converts date string from bbc html to publication date in subtracting 29 days
     * e.G. Wed 23 September 2020, 22:00 will result in Tuesday, 25 August 2020
     *
     * @param dateInString
     * @param dateOffset days to subtract from date
     * @return
     */
    private static String getPublicationDate(String dateInString, int dateOffset) {

        DateFormat format = new SimpleDateFormat("E d MMMM yyyy, HH:mm", Locale.ENGLISH);
        try {

            Date date = format.parse(dateInString);
            date = getDate(date, dateOffset, "subtract");
            return format.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Mon 01 Januar 0000, 00:00";
        }

    }

    @NonNull
    private static Date getDate(Date date, int offset, String theProcess) {
        Calendar cal = Calendar.getInstance();
        assert date != null;
        cal.setTime(date);
        if(theProcess.equals("add"))
            cal.add(Calendar.DATE, + offset);
        else
            cal.add(Calendar.DATE, - offset);
        date = cal.getTime();
        return date;
    }

    /**
     * Creates an Intent for DownloadService for Downloading a single podcast
     *
     * @param item                current item
     * @param theContext
     * @param isToastOnFileExists If we need notification and toast on the info that there is a file of the given name already sitting in filesystem
     * @param isStartedInBackground whether this service will be started from background task
     * @return
     */
    public Intent prepareItemDownload(DownloadListItem item, Context theContext, boolean isToastOnFileExists, boolean isStartedInBackground, String theProgram, int rowNumber) {
        Bundle bundle = new Bundle();
        bundle.putString("url", item.url);

        bundle.putString("fileName", item.fileName);
        bundle.putBoolean("isToastOnFileExists", isToastOnFileExists);
        bundle.putBoolean("isStartedInBackground", isStartedInBackground);
        bundle.putString("theProgram", theProgram);
        bundle.putInt("button_id", rowNumber);
        Log.d("DOWNLOAD_UTILS", "prepareItemDownload() ID of button: "+bundle.getInt("button_id"));
        Intent intent = new Intent(theContext, DownloadService.class);
        intent.putExtras(bundle);
        return intent;

    }

    /**
     * @param theContentDesc
     * @param theDate
     * @return
     */
    private String prepareFilename(String theContentDesc, String theDate, String theProgram) {
        theDate = theDate.replace(",", "");
        theDate = theDate.replaceAll(" ", "_");
        theDate = theDate.replaceAll(":", "_");
        theContentDesc = theContentDesc.replace("?", "");
        theContentDesc = theContentDesc.replaceAll(" ", "_");
        theContentDesc = theContentDesc.replaceAll(":", "_");
        theContentDesc = replaceInName(theContentDesc, "'", "_");
        theContentDesc = replaceInName(theContentDesc, "\"", "");
        theContentDesc = replaceInName(theContentDesc, ",", "");
        theContentDesc= replaceInName(theContentDesc,"__", "_");

        String startOfFilename = StringUtils.capitalize(theProgram);

        if(theContentDesc.startsWith(startOfFilename))
            return theContentDesc.replace(startOfFilename, startOfFilename+"_")+"_"+theDate+".mp3";
        else
            return startOfFilename+"_"+theContentDesc+"_"+theDate+".mp3";
    }

    @NonNull
    private String replaceInName(String theContentDesc, String s, String s2) {
        for (int i = 0; i < 10; i++) {
            if (theContentDesc.indexOf(s) == -1)
                break;
            theContentDesc = theContentDesc.replace(s, s2);
        }
        return theContentDesc;
    }


    /**
     * Check for existance of given filename in podcasts directory
     *
     * @param fileName
     * @return
     */
    public File fileExists(String fileName, Context context, String theProgram) {
        String root = PreferenceManager.getDefaultSharedPreferences(context).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());
        File myDir = new File(root+"/"+theProgram );
        if (!myDir.exists()) {
            return null;
        }
        File theFile = new File(root +"/"+theProgram+"/"+fileName);
        if (theFile.exists()) {
            return theFile;
        }
        return null;
    }
    public void showNotification(String title, String message, Context context, NotificationManager mNotificationManager) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1234567",
                    "BBC_POD_DL",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Download podcasts ongoing");
            mNotificationManager.createNotificationChannel(channel);
        }
        // Create an explicit intent for an SettingsActivity => Entrypoint of the app
        Intent intent = new Intent(context, SettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "1234567")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(message)// message for notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true); // clear notification after click
        mNotificationManager.notify(0, mBuilder.build());
    }
    /*
     * Method to check storage options
     * ContextCompat.getExternalFilesDirs(context, null) gives an array of File
     * position [0] always points to the internal root dir
     *
     * */
    public static CharSequence[] getStoragePaths(Context context) {

        if(ContextCompat.getExternalFilesDirs(context, null).length >=2 )
            return addToDirArry(2, ContextCompat.getExternalFilesDirs(context, null));
        else
            return addToDirArry(1, ContextCompat.getExternalFilesDirs(context, null));


    }
    private static String[] addToDirArry(int count, File[] fileArry){
        List<String> resultList = new LinkedList<String>();
        for(int i=0; i < count;i++){
            resultList.add(fileArry[i].getAbsolutePath());
        }
        String[] resultArry = new String[resultList.size()];
        for(int i=0;i<resultList.size();i++)
            resultArry[i] = resultList.get(i);
        return resultArry;

    }

    public Intent getSettingsIntend(Context context) {
        Intent intent_settings = new Intent(context, SettingsActivity.class);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("show_settings", true).apply();
        editor.commit();
        return intent_settings;
    }

    @Nullable
    public Bundle getDownloadOptionsBundle(Context context, String theProgram){

        Bundle theDownloadedPodcastBundle;
        try {
            theDownloadedPodcastBundle = this.getDownloadedPodcastsBundle(context, theProgram);
        } catch (IOException e) {
            e.printStackTrace();
            theDownloadedPodcastBundle = null;
        }
        Bundle downloadOptionsBundle = this.getCurrentDownloadOptions(context, theProgram);


        if(theDownloadedPodcastBundle!=null){
            if(downloadOptionsBundle!=null)
                mergeBundles(theDownloadedPodcastBundle, downloadOptionsBundle);
            else
                //Just use already downloaded podcasts if not connected to internet
                downloadOptionsBundle = theDownloadedPodcastBundle;
        }
        return downloadOptionsBundle;
    }

    /**
     * merge fresh download options with already downloaded item options
     * @param theDownloadedPodcastBundle
     * @param downloadOptionsBundle
     */
    private void mergeBundles(Bundle theDownloadedPodcastBundle, Bundle downloadOptionsBundle) {
        int theSizeOfDownloadOptions = downloadOptionsBundle.getInt("LIST_SIZE");
        int theSizeOfDownloadedPodcasts = theDownloadedPodcastBundle.getInt("LIST_SIZE");
        int sizeAll = theSizeOfDownloadOptions;

        for (int i=0;i<theSizeOfDownloadedPodcasts;i++){
            DownloadListItem item = theDownloadedPodcastBundle.getParcelable("ITEM_"+i);

            boolean isFound = false;
            for (int j=0;j<theSizeOfDownloadOptions;j++){
                DownloadListItem itemOptions = downloadOptionsBundle.getParcelable("ITEM_"+j);
                if(item.fileName!=null && itemOptions.fileName!=null)
                    if(item.fileName.equals(itemOptions.fileName)){
                        downloadOptionsBundle.remove("ITEM_" + j);
                        //Neuer ITEM, weil url final ist, somit nicht auf "none" gesetzt werden kann, also ersetzen
                        //DownloadListItem itemTemp = new DownloadListItem(String.valueOf(j), itemOptions.content, "none", itemOptions.dateOfPublication, itemOptions.fileName);
                        downloadOptionsBundle.putParcelable("ITEM_" + j, item);

                        isFound = true;
                        break;
                    }
            }
            //Nicht gefunden, anhaengen an original download optionss
            if(isFound==false){
                sizeAll++;
                downloadOptionsBundle.putParcelable("ITEM_"+sizeAll, item);
            }
        }
        downloadOptionsBundle.remove("LIST_SIZE");
        sizeAll++;
        downloadOptionsBundle.putInt("LIST_SIZE",sizeAll);
    }

    public static void checkDir(File myDir, Context context) {
        if (!myDir.exists()) {
            if(!myDir.mkdirs()){
                Toast.makeText(context, "Directory "+ myDir +" not created, switch directory settings to internal storage", Toast.LENGTH_LONG).show();
            }
        }
    }

    public  void processChoosenDownloadOptions(Context theContext) {
        Log.d("UTIL", "processChoosenDownloadOptions start");

        //If empty create new Downloadrequests
        if(downLoadRequestMap.isEmpty()){
            Log.d("UTIL", "processChoosenDownloadOptions init background");
            for (String currentProgram : URL_MAP.keySet()) {
                downLoadRequestMap.put(currentProgram, getDownLoadRequest(currentProgram));
            }

        }
        //If download in background has been choosen, then shedule it
        if(PreferenceManager.getDefaultSharedPreferences(theContext).getBoolean("dl_background", true)==true){
            Log.d("UTIL", "processChoosenDownloadOptions schedule background");
            for (PeriodicWorkRequest currentDownloadRequest : downLoadRequestMap.values()) {
                WorkManager
                        .getInstance(theContext)
                        .enqueue(currentDownloadRequest);
            }
        }else{
            //cancel all background downloads, if there are any
            for (PeriodicWorkRequest currentDownloadRequest : downLoadRequestMap.values()) {
                Log.d("UTIL", "processChoosenDownloadOptions cancel background");
                WorkManager
                        .getInstance(theContext)
                        .cancelWorkById(currentDownloadRequest.getId());
            }
        }

        Log.d("UTIL", "processChoosenDownloadOptions end");
    }


    public void startListService(Context theContext, String theProgram, int httpCode, Class theClass){


        Intent intent = new Intent(theContext, theClass);
        intent.putExtra("http_error_code", httpCode);
        intent.putExtra("theProgram", theProgram);


        theContext.startService(intent);
    }

    public void startRadioLive(Context context){
        Log.d("UTIL", "startRadioLive start");

        RadioLive  bbcWorldserviceLive = RadioLive.getInstance();
     if(bbcWorldserviceLive.isInitial==false){
         if(!bbcWorldserviceLive.isPlaying()){
             Log.d("UTIL", "startRadioLive reset and then initialization of Mediaplayer");

             bbcWorldserviceLive.reset();
             bbcWorldserviceLive.initMplayer(context,URL_MAP.get(PROGRAM_RADIOLIVE));
         }
         return;
     }
     Log.d("UTIL", "startRadioLive initialization of Mediaplayer");
     bbcWorldserviceLive.initMplayer(context, URL_MAP.get(PROGRAM_RADIOLIVE));
    }



}
