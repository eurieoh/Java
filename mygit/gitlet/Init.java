package gitlet;
import java.io.File;

/** Init class makes the folders.
 * @author Eurie Oh and Caroline Kim */
class Init {
    /** Unimportant string to get Init running. */
    private String _idk;
    /** Sets _idk to the input wat to run Init.
    * @param wat takes in a string */
    Init(String wat) {
        _idk = wat;
    }
    /** When create is called, the new folders are made.
    * @return returns Commits */
    public Commits create() {
        if (new File(".gitlet").exists()) {
            System.out.print("A gitlet version-control system");
            System.out.println(" already exists in the current directory.");
            return null;
        }
        File gitlet = new File(".gitlet");
        gitlet.mkdir();
        File staged = new File(".gitlet/staged");
        staged.mkdir();
        File removed = new File(".gitlet/removed");
        removed.mkdir();
        File dummy = new File(".gitlet/dummy");
        dummy.mkdir();
        Commits c = new Commits(null, null, "initial commit");
        return c;
    }
}
