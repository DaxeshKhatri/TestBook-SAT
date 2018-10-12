package in.com.testbook.chatapp.Models;


public class Users {

    public String fname;



    public String lname;
    public String image;
    public String status;
    public String thumb_image;



    public Users(){

    }

    public Users(String name,String lastname, String image, String status, String thumb_image) {
        this.fname = name;
        this.lname=lastname;
        this.image = image;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

}
