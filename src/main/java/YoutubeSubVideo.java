import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.*;
import com.google.api.services.youtube.YouTube;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;


public class YoutubeSubVideo {

    /** Application name. */
    private static final String APPLICATION_NAME = "youtubeSubVideo";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(".credentials/youtube-java-subscrible-video");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;


    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials
     */
    private static final List<String> SCOPES =
            Arrays.asList(YouTubeScopes.YOUTUBE_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Create an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                YoutubeSubVideo.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    /**
     * Build and return an authorized API client service, such as a YouTube
     * Data API client service.
     * @return an authorized API client service
     * @throws IOException
     */
    public static YouTube getYouTubeService() throws IOException {
        Credential credential = authorize();
        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {
        YouTube youtube = getYouTubeService();
        try {
                HashMap<String, String> parameters = new HashMap<String, String>();
                parameters.put("part", "snippet,contentDetails");
                parameters.put("vid", "id,snippet");
                YouTube.Subscriptions.List mySubscribeList = youtube.subscriptions().list(parameters.get("part")).setMine(true);
                SubscriptionListResponse response = mySubscribeList.execute();
                for (Subscription channel: response.getItems()) {
                    YouTube.Search.List videos = youtube.search().list(parameters.get("vid"))
                                                                .setChannelId(channel.getSnippet().getResourceId().getChannelId())
                                                                .setMaxResults(Long.parseLong("50"));
                    SearchListResponse videoResponse = videos.execute();
                    SearchResult  latestVideo = getLatestVideo(videoResponse.getItems());
                    System.out.println("________________________________________");
                    System.out.println("Channel title : "+ channel.getSnippet().getTitle());
                    System.out.println("ChannelId : "+ channel.getSnippet().getResourceId().getChannelId());
                    System.out.println("Lastest Video title : " + latestVideo.getSnippet().getTitle());
                    System.out.println("Link: " + "https://www.youtube.com/watch?v=" + latestVideo.getId().getVideoId());
                }
        } catch (GoogleJsonResponseException e) {
            e.printStackTrace();
            System.err.println("There was a service error: " +
                    e.getDetails().getCode() + " : " + e.getDetails().getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static SearchResult getLatestVideo(List<SearchResult> target){
        SearchResult searchResult = target.get(0);
        for(int i=1; i<target.size(); i++){
            if(searchResult.getSnippet().getPublishedAt().getValue() < target.get(i).getSnippet().getPublishedAt().getValue()){
                searchResult = target.get(i);
            }
        }
        return searchResult;
    }
}