public class Product {
    private String url;
    private float desiredPrice;
    private boolean alreadySent;

    public Product(String url, float desiredPrice) {
        this.url = url;
        this.desiredPrice = desiredPrice;
        alreadySent = false;
    }

    public String getUrl() {
        return url;
    }

    public float getDesiredPrice() {
        return desiredPrice;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDesiredPrice(float price) {
        this.desiredPrice = price;
    }

    /**
     * @return current price if it is lower than desiredPrice
     * @return -1 if current price is higher than desiredPrice
     */
    public float isLower() {
        PriceChecker c = new PriceChecker();
        float currentPrice = c.getPrice(url);
        if (currentPrice <= desiredPrice) {
            return currentPrice;
        }
        else {
            //if current price is higher than desired, reset alreadySent to false
            alreadySent = false;
            return -1;
        }
    }

    public boolean isAlreadySent(){
        if(alreadySent == false)
            return false;
        else
            return true;
    }
    public void setAlreadySent(boolean value){
        alreadySent = value;
    }
}



