import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        final int MINUTES_TO_WAIT = 240; // every 4 hours

        ArrayList<User> users = new ArrayList<>();
        loadExistingUsers(users);

        try {
            //registering telegram bot
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            TrackerBot tBot = new TrackerBot(users);
            botsApi.registerBot(tBot);

            //check price loop
            while (true) {
                for (User u : users) {
                    System.out.println("Checking prices of user:"+u.getChatid());
                    for (Product p : u.carrello.products) {
                        //remove product if i cannot get the price
                        float newprice = p.isLower();
                        if (newprice == -1){
                            tBot.sendMSG("Error: cannot retrieve price of product "+p.getUrl()+" so it will be removed",u.getChatid());
                            u.carrello.products.remove(p);
                        }
                        if (newprice != -1 && p.isAlreadySent() == false) {
                            tBot.sendMSG("Discount detected!", u.getChatid());
                            tBot.sendMSG("New price: €" + newprice + "  " + p.getUrl(), u.getChatid());
                            p.setAlreadySent(true);
                            //waiting 5 seconds so amazon doesn't get suspicious
                            Thread.sleep(5000);
                        }
                    }
                }
                Thread.sleep(MINUTES_TO_WAIT*60000);
            }


        } catch (TelegramApiException | InterruptedException e) {
            System.err.println("Program is not in loop");
            e.printStackTrace();
        }
    }


    private static void loadExistingUsers(ArrayList<User> users) {
        System.out.println("Loading existing users..");
        try {
            File database = new File("src/main/database/userlist.txt");
            Scanner myReader = new Scanner(database);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                users.add(new User(line));
            }
            myReader.close();
            System.out.println("User list loaded");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
