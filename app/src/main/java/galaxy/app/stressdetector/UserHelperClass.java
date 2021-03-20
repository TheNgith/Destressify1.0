package galaxy.app.stressdetector;

public class UserHelperClass {

    String bPM, aVNN, sDNN, rMSSD, predStress, stress, pam;

    public UserHelperClass() {
    }

    public UserHelperClass(String bPM, String aVNN, String sDNN, String rMSSD, String predStress, String stress, String pam) {
        this.bPM = bPM;
        this.aVNN = aVNN;
        this.sDNN = sDNN;
        this.rMSSD = rMSSD;
        this.predStress = predStress;
        this.stress = stress;
        this.pam = pam;
    }

    public String getbPM() {
        return bPM;
    }

    public void setbPM(String bPM) {
        this.bPM = bPM;
    }

    public String getaVNN() {
        return aVNN;
    }

    public void setaVNN(String aVNN) {
        this.aVNN = aVNN;
    }

    public String getsDNN() {
        return sDNN;
    }

    public void setsDNN(String sDNN) {
        this.sDNN = sDNN;
    }

    public String getrMSSD() {
        return rMSSD;
    }

    public void setrMSSD(String rMSSD) {
        this.rMSSD = rMSSD;
    }

    public String getPredStress() {
        return predStress;
    }

    public void setPredStress(String predStress) {
        this.predStress = predStress;
    }

    public String getStress() {
        return stress;
    }

    public void setStress(String stress) {
        this.stress = stress;
    }

    public String getPam() {
        return pam;
    }

    public void setPam(String pam) {
        this.pam = pam;
    }
}
