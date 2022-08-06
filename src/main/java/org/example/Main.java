package org.example;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.SortTerm;
import org.json.JSONObject;
import javax.mail.*;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws MessagingException, IOException {
        final Message lastMessageForLunchMenu = getLastMessageForLunchMenu();

        final String subject = lastMessageForLunchMenu.getSubject();
        final Date receiveDate = lastMessageForLunchMenu.getReceivedDate();
        try {
            notifyForLunchMenu(subject, receiveDate);
        }catch(IOException e){
            System.out.println(e);
        }

    }
    private static void notifyForLunchMenu(final String lastMenuEmailSubject, final Date lastMenuEmailDate) throws IOException {
        //check if there is a new mail and notify
        //to do that we need to persist the last state -
        //the last state is the date of the last email
        //we send notification for



        //convert lastMenuEmailDate to LocalDateTime and compare it with a stored value
        //Where to store the state?
        //- in the users folder

        final String homeFolder = System.getProperty("user.home");
        final String stateFileName = "lastLunchMenuEmailDate";

       final File stateFile = new File(homeFolder + "\\" + stateFileName);
        if(!stateFile.exists()){
            //the program executes for the first time or the file has been detected
            //create file and notify
            final boolean fileCreationResult = stateFile.createNewFile();
            final String message="¬ÌËÏ‡ÌËÂ ¬ÌËÏ‡ÌËÂ! \n Œ¡≈ƒÕŒ“Œ Ã≈Õﬁ «¿ —À≈ƒ¬¿Ÿ¿“¿ —≈ƒÃ»÷¿ ≈  ¿◊≈ÕŒ! \n [«¿œ¿«» “” ](PUT HYPERLINK HERE)";
            final LocalDateTime lastMenuLocalDateTime = lastMenuEmailDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            if(!fileCreationResult){
                throw new RuntimeException("Cannot create the state file. Exit.");

            }

            //store the date from the email in ISO format

           // standardEmailDate(lastMenuEmailDate, stateFile);
            //makeRequestToMattermost(message);

        }
        else{
            //the file exists
            //read the file and get the date for the last notification
            // if the currents email date is after the last notification, then notify and store the new date
            try (final FileInputStream stateInputStream= new FileInputStream(stateFile)){
                final byte[] bytes=stateInputStream.readAllBytes();
                final String message="¬ÌËÏ‡ÌËÂ ¬ÌËÏ‡ÌËÂ! \n Œ¡≈ƒÕŒ“Œ Ã≈Õﬁ «¿ —À≈ƒ¬¿Ÿ¿“¿ —≈ƒÃ»÷¿ ≈  ¿◊≈ÕŒ! \n [«¿œ¿«» “” ]([PUT HYPERLINK HERE])";
                final String lastEmailDate = new String(bytes, StandardCharsets.UTF_8);
                final LocalDateTime lastStoredEmailDateTime = LocalDateTime.parse(lastEmailDate,
                        DateTimeFormatter.ISO_DATE_TIME);
                //final String s=new String(bytes,StandardCharsets.UTF_8);
                if (lastStoredEmailDateTime.isAfter(lastStoredEmailDateTime)) {
                    System.out.println("No lunch");
                }
//                standardEmailDate(lastMenuEmailDate, stateFile);
//                makeRequestToMattermost(message);
                standardEmailDate(lastMenuEmailDate, stateFile);
            makeRequestToMattermost(message);

            }

        }



        //makeRequestToMattermost("aaaa");

    }

    private static void standardEmailDate(Date lastMenuEmailDate, File stateFile) throws IOException {
        LocalDateTime lastMenuLocalDateTime = LocalDateTime.from(lastMenuEmailDate.toInstant()
                .atZone(ZoneId.systemDefault())).toLocalDate().atStartOfDay();
        //format the date
        final String lastMenuLocalDateTimeFormatted = DateTimeFormatter.ISO_DATE_TIME.format(lastMenuLocalDateTime);
        try(final FileOutputStream stateFileOutputStream = new FileOutputStream(stateFile)){
            //convert the string to bytes
            stateFileOutputStream.write(lastMenuLocalDateTimeFormatted.getBytes(StandardCharsets.UTF_8));


        }
    }

    private static Message getLastMessageForLunchMenu() throws UnsupportedEncodingException {
            WinCred wc = new WinCred();


            WinCred.Credential cred = wc.getCredential("methodialdap");
            String username = cred.getUsername();//getUsername()
            System.out.println(username);


            Session session = Session.getDefaultInstance(new Properties());

            Store store = null;

            try {
                store = session.getStore("imaps");
                store.connect("PUT HOST HERE", 993, "PUT EMAIL HERE", cred.getPassword());
               Folder inbox = store.getFolder("lunchmenu");
                inbox.open(Folder.READ_ONLY);
                SortTerm sortTerm[] = new SortTerm[] { SortTerm.REVERSE, SortTerm.DATE };

                Message[] messages = ((IMAPFolder) inbox).getSortedMessages(sortTerm);
                final Message[] message =inbox.getMessages();
                final Message lastMessage= message[message.length-1];
                final String lastMessageSubject=lastMessage.getSubject();
                System.out.println(lastMessageSubject);
                return lastMessage;
            }catch(MessagingException e){
                throw new RuntimeException("Cannot read email");
            }
        }
        private static void makeRequestToMattermost(String message){
        final JSONObject request = new JSONObject();
        request.put("text", message);
        HttpRequest mattermostRequest=HttpRequest.newBuilder()
                .header("Content-type", "application/json")
                .uri(URI.create("PUT THE LINK WHERE YOU WOULD LIKE TO REQUEST FROM"))
                .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
                        .build();

        final HttpClient httpClient = HttpClient.newHttpClient();
        try {
            final HttpResponse<Void> response = httpClient.send(mattermostRequest, HttpResponse.BodyHandlers.discarding());
            System.out.println(response);
        }catch(IOException | InterruptedException e){
            throw new RuntimeException("Cant send request to mattermost...", e);
        }
    }
}
