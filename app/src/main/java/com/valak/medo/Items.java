package com.valak.medo;

public class Items {

    public String name, image, packing;
    long price;

    public Items() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPacking() {
        return packing;
    }

    public void setPacking(String packing) {
        this.packing = packing;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public Items(String name, String image, String packing, long price) {
        this.name = name;
        this.image = image;
        this.packing = packing;
        this.price = price;
    }
}
