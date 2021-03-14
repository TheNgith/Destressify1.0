package galaxy.app.stressdetector;

public class UserHelperClassContact {
    String name, rela, info;

    public UserHelperClassContact() {

    }

    public UserHelperClassContact(String name, String rela, String info) {
        this.name = name;
        this.rela = rela;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRela() {
        return rela;
    }

    public void setRela(String rela) {
        this.rela = rela;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
