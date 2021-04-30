import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;

public class User {
    private String chatid;
    ProductList carrello;
    StateOfConversation state;

    public User(String chatid) {
        this.chatid = chatid;
        carrello = new ProductList();
        state = new StateOfConversation();
        loadDatabase();
    }

    public String getChatid() {
        return chatid;
    }

    public void loadDatabase() {
        System.out.println("Loading database..");
        try {
            File database = new File("src/main/database/" + this.chatid + ".txt");
            if (database.createNewFile()) {
                System.out.println("File created: " + database.getName());
            } else {
                System.out.println("File already exists for user: " + chatid);
            }
            Scanner myReader = new Scanner(database);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                StringTokenizer st = new StringTokenizer(line, " ");
                String url = st.nextToken();
                float desiredPrice = Float.parseFloat(st.nextToken());
                carrello.products.add(new Product(url, desiredPrice));
            }
            myReader.close();
            System.out.println("Database successfully loaded for user: " + chatid);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDatabase() {
        //delete old file
        File myObj = new File("src/main/database/" + this.chatid + ".txt");
        if (myObj.delete()) {
            //System.out.println("Deleted the file: " + myObj.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }

        //create new file
        try {
            File database = new File("src/main/database/" + this.chatid + ".txt");
            if (database.createNewFile()) {
                //System.out.println("File created: " + database.getName());
            } else {
                //System.out.println("File already exists for user: " + chatid + " this should not be happening");
            }

            //put products in file
            try {
                FileWriter fstream = new FileWriter("src/main/database/" + this.chatid + ".txt");
                BufferedWriter info = new BufferedWriter(fstream);
                for (Product p : carrello.products) {
                    info.write(p.getUrl() + " " + p.getDesiredPrice());
                    info.newLine();
                }
                info.close();
            } catch (Exception e) {
                System.out.println("A write error has occurred");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
