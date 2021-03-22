package galaxy.app.destressify;

public class UserHelperClassEveryday {

    String sleep, walk;

    public UserHelperClassEveryday() {
    }

    public UserHelperClassEveryday(String sleep, String walk) {
        this.sleep = sleep;
        this.walk = walk;
    }

    public String getSleep() {
        return sleep;
    }

    public void setSleep(String sleep) {
        this.sleep = sleep;
    }

    public String getWalk() {
        return walk;
    }

    public void setWalk(String walk) {
        this.walk = walk;
    }
}
