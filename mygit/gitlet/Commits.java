package gitlet;
import java.util.Calendar;
import java.io.Serializable;
import java.util.ArrayList;
import java.io.File;
import java.util.List;
/** Commits class stores the blob information.
 * @author Eurie Oh and Caroline Kim */
class Commits implements Serializable {

    /** Constructor for Commits.
     * @param blob is the blobs to be commited
     * @param par is the parent of the commit
     * @param message is the message for this commit */
    public Commits(ArrayList<Blob> blob, Commits par, String message) {
        _time = time();
        if (blob != null) {
            _blobtree = blob;
            for (Blob b : _blobtree) {
                _bnames.add(b.name());
                _h.add(b.hash());
            }
        }
        if (par == null) {
            _parent = null;
        } else {
            _parent = par.hash();
        }
        _message = message;
        setver(par);
        if (!_h.isEmpty()) {
            File f = new File("caroline.txt");
            String p;
            if (par == null) {
                p = "null";
            } else {
                p = par.hash();
            }
            String w = _message + _time + p;
            Utils.writeContents(f, w.getBytes());
            Blob t = new Blob(f, message);
            _h.add(t.hash());
            _hashcode = Utils.sha1(_h.toArray());
            _h.remove(t.hash());
            f.delete();
        } else {
            File f = new File("caroline.txt");
            String p;
            if (par == null) {
                p = "null";
            } else {
                p = par.hash();
            }
            String w = _message + _time + p;
            Utils.writeContents(f, w.getBytes());
            Blob t = new Blob(f, message);
            _h.add(t.hash());
            _hashcode = Utils.sha1(_h.toArray());
            _h.remove(t.hash());
            f.delete();
        }
    }

    /** Says whether two commits are the same.
     * @param c is the Commit that you are comparing to
     * @return a boolean of whether they are equal */
    Boolean equals(Commits c) {
        Boolean eq = c.hash().equals(_hashcode);
        ArrayList<Blob> cb = c.bloblist();
        for (Blob b : _blobtree) {
            if (!c.contains(b)) {
                return false;
            }
        }
        return eq;
    }

    /** Gives the names of the blobs for this commit.
     * @return the blob names as an arraylist */
    ArrayList<String> bnames() {
        return _bnames;
    }

    /** Gives the commits hashcode.
     * @return the hashcode as a string */
    String hash() {
        return _hashcode;
    }

    /** Gives the blobs for this commit.
     * @return the blobs in an arraylist */
    ArrayList<Blob> bloblist() {
        return _blobtree;
    }

    /** Determines when the commit was made.
     * @return the time as a string */
    String time() {
        Calendar cal = Calendar.getInstance();
        String t = "";
        int n = cal.get(Calendar.YEAR);
        t = t + Integer.toString(n) + "-";
        n = cal.get(Calendar.MONTH) + 1;
        if (n < 10) {
            t = t + "0" + Integer.toString(n) + "-";
        } else {
            t = t + n + "-";
        }
        n = cal.get(Calendar.DATE);
        if (n < 10) {
            t = t + "0" + Integer.toString(n) + " ";
        } else {
            t = t + n + " ";
        }
        n = cal.get(Calendar.HOUR_OF_DAY);
        if (n < 10) {
            t = t + "0" + Integer.toString(n) + ":";
        } else {
            t = t + n + ":";
        }
        n = cal.get(Calendar.MINUTE);
        if (n < 10) {
            t = t + "0" + Integer.toString(n) + ":";
        } else {
            t = t + Integer.toString(n) + ":";
        }
        n = cal.get(Calendar.SECOND);
        if (n < 10) {
            t = t + "0" + Integer.toString(n);
        } else {
            t = t + Integer.toString(n);
        }
        return t;
    }

    /** Gives the version of the commit.
     * @return the version as an int */
    int version() {
        return _version;
    }

    /** determines the version of the commit.
     * @param par is the parent */
    void setver(Commits par) {
        if (par != null) {
            _version = par.version() + 1;
        }
        _version = 1;
    }

    /** Gives the message of this commit.
     * @return the message as a String */
    String message() {
        return _message;
    }

    /** Gives the parent hashcode.
     * @return the hashcode as a string */
    String parent() {
        return _parent;
    }

    /** Determines whether this commit has this blob.
     * @param x is the blob you're considering
     * @return true or false */
    Boolean contains(Blob x) {
        for (Blob b : _blobtree) {
            if (b.equals(x)) {
                return true;
            }
        }
        return false;
    }

    /** Determines whether this blob is in this commit.
     * @param x is the name of the blob
     * @return true or false of whether this commit has it */
    Boolean contains(String x) {
        for (String b : _bnames) {
            if (_bnames.contains(x)) {
                return true;
            }
        }
        return false;
    }

    /** Gives the blob of this commit with this name.
     * @param name is the name of the blob
     * @return the blob if it has it or null if not */
    Blob get(String name) {
        for (Blob b : _blobtree) {
            if (b.name().equals(name)) {
                return b;
            }
        }
        return null;
    }

    /** An arraylist of blobs. */
    private ArrayList<Blob> _blobtree = new ArrayList<Blob>();

    /** An arraylist of blob names. */
    private ArrayList<String> _bnames = new ArrayList<String>();

    /** A list of blob hashcodes. */
    private List<String> _h = new ArrayList<String>();

    /**The time of this commit. */
    private String _time;

    /** THe hashcode of the parent.*/
    private String _parent;

    /** This commit's hashcode. */
    private String _hashcode;

    /** The version of this commit. */
    private int _version;

    /** The message of this commit. */
    private String _message;
}
