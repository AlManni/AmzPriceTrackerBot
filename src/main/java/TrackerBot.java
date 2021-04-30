import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TrackerBot extends TelegramLongPollingBot {
    private ArrayList<User> users;

    public TrackerBot(ArrayList<User> users) {
        this.users = users;
    }

    @Override
    public String getBotUsername() {
        return "INSERT BOT NAME HERE";
    }

    @Override
    public String getBotToken() {
        return "INSERT BOT TOKEN HERE";
    }

    @Override
    public void onUpdateReceived(Update update) {
        String chatid = update.getMessage().getChatId().toString();
        System.out.println("chatid received: " + chatid);
        int indexOfUser = -1;
        //searching index of user with said chatid in ArrayList "users"
        indexOfUser = getIndexOfUser(chatid);

        //if indexOfUser is still = -1, user is not in the list, so it has to be added to Arraylist "users"
        //after that, we have to re-calculate indexOfUser
        if (indexOfUser == -1) {
            //add user
            users.add(new User(chatid));
            //add user's chatid to userlist.txt, so it can be automatically loaded on next reboot
            addNewUserToExistingUserList(chatid);
        }
        indexOfUser = getIndexOfUser(chatid);

        //sending message to conversationHandler
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println("message received:" + update.getMessage().getText());
            conversationHandler(update.getMessage().getText(), chatid, indexOfUser);
        }
    }

    private int getIndexOfUser(String chatid) {
        int indexOfUser = -1;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getChatid().equals(chatid)) {
                indexOfUser = i;
                break;
            }
        }
        return indexOfUser;
    }

    private void conversationHandler(String msgReceived, String chatid, int indexOfUser) {
        switch (users.get(indexOfUser).state.getState()) {
            case 0: //expecting new instruction
                case0Handler(msgReceived, chatid, indexOfUser);
                break;
            case 1: //expecting url to add
                case1Handler(msgReceived, chatid, indexOfUser);
                break;
            case 2: // expecting desiredPrice of product to add
                case2Handler(msgReceived, chatid, indexOfUser);
                break;
            case 3: // expecting id of product to remove
                case3Handler(msgReceived, chatid, indexOfUser);
                break;
        }
    }

    private void case0Handler(String msgReceived, String chatid, int indexOfUser) {
        switch (msgReceived) {
            case "/start":
                sendMSG("Welcome to the Monkey Amazon Tracker Bot! Type /add to add a product, /remove to remove a product and /list to show your products", chatid);
                break;
            case "/whoami":
                sendMSG(chatid, chatid);
                break;
            case "/add":
                sendMSG("Send URL of your product", chatid);
                users.get(indexOfUser).state.setState(1);
                break;
            case "/remove":
                if (users.get(indexOfUser).carrello.products.size() != 0) {
                    sendMSG("Send the [id] of the product you want to remove (1 -> " + users.get(indexOfUser).carrello.products.size() + ")", chatid);
                    users.get(indexOfUser).state.setState(3);
                } else {
                    sendMSG("You have no products on watchlist, type /add to add one",chatid);
                }
                break;
            case "/list":
                sendListOfProducts(chatid, indexOfUser);
                break;
            default:
                sendMSG("Command not found, type /add to add a product, /remove to remove a product and /list to show your products", chatid);
        }
    }

    //expecting url to add (string)
    private void case1Handler(String msgReceived, String chatid, int indexOfUser) {
        //only add max 10 products for bandwith reasons
        if(users.get(indexOfUser).carrello.products.size() >= 10) {
            sendMSG("You have reached your max number of products, please remove one first with /remove", chatid);
            users.get(indexOfUser).state.setState(0);
        }
        else if (msgReceived.contains("https://www.amazon")) {
            users.get(indexOfUser).carrello.newUrl = msgReceived;
            sendMSG("Set desired price", chatid);
            users.get(indexOfUser).state.setState(2);
        } else {
            sendMSG("Invalid url, type /add to try again", chatid);
            users.get(indexOfUser).state.setState(0);
        }

    }

    //expecting desiredPrice (float) of product to add
    private void case2Handler(String msgReceived, String chatid, int indexOfUser) {
        try {
            users.get(indexOfUser).carrello.newPrice = Float.parseFloat(msgReceived);
            //add temp product to carrello Arraylist of user
            users.get(indexOfUser).carrello.products.add(new Product(users.get(indexOfUser).carrello.newUrl, users.get(indexOfUser).carrello.newPrice));
            users.get(indexOfUser).updateDatabase();
            users.get(indexOfUser).state.setState(0);
            sendMSG("Product added to watchlist!", chatid);
        } catch (NumberFormatException e) {
            sendMSG("Error: not a number, please send a valid number", chatid);
        }

    }

    //expecting position of the product in arraylist to remove (0 -> products.size)
    private void case3Handler(String msgReceived, String chatid, int indexOfUser) {
        try {
            int i = Integer.parseInt(msgReceived);
            //user input is shifted +1, so we have to decrement to search the array
            i--;
            if (i >= 0 && i < users.get(indexOfUser).carrello.products.size()) {
                users.get(indexOfUser).carrello.products.remove(i);
                users.get(indexOfUser).updateDatabase();
                //user is expecting i+1 because user start counting from 1
                //if you add code later in this method, remember to decrement i by 1
                i++;
                sendMSG("Product [" + i + "] removed from list", chatid);
            } else {
                sendMSG("Invalid position, type /remove to try again and /list to view your products", chatid);
            }
        } catch (NumberFormatException e) {
            sendMSG("Error: not a valid number, send /remove to try again", chatid);
        }
        users.get(indexOfUser).state.setState(0);
    }

    public void sendMSG(String msg, String chatid) {
        SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
        message.setChatId(String.valueOf(chatid));
        message.setText(msg);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        System.out.println("sending to " + chatid + ": " + msg);
    }

    private void sendListOfProducts(String chatid, int indexOfUser) {
        if (users.get(indexOfUser).carrello.products.size() == 0) {
            sendMSG("You have not added any product, type /add to add one", chatid);
        } else {
            for (int i = 0; i < users.get(indexOfUser).carrello.products.size(); i++) {
                int showIndex = i+1;
                sendMSG("[" + showIndex + "] --> Price set: €" + users.get(indexOfUser).carrello.products.get(i).getDesiredPrice()
                        + "  " + users.get(indexOfUser).carrello.products.get(i).getUrl(), chatid);
            }
        }
    }

    private void addNewUserToExistingUserList(String id) {
        try (FileWriter fw = new FileWriter("src/main/database/userlist.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
