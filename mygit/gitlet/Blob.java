package gitlet;
import java.io.File;
import java.io.Serializable;

/** Blob class stores the file information.
 * @author Eurie Oh and Caroline Kim */
class Blob implements Serializable {

    /** This is the string hash.*/
    private String _hash;

    /** This is the file name.*/
    private String _name;

    /** This is the file content as a byte.*/
    private byte[] _content;

    /** The constructor of the BLob.
     * @param f is the File you want to make into a blob
     * @param name is the name of the file*/
    public Blob(File f, String name) {
        _name = name;
        _content = Utils.readContents(f);
        _hash = Utils.sha1(_content);
    }

    /** Gives the hashcode of the file.
     * @returns the hashcode as a string*/
    String hash() {
        return _hash;
    }

    /** Gives the file name.
     * @return the name as a string*/
    String name() {
        return _name;
    }

    /** Gives the file information.
     * @return the byte[] form of the content*/
    byte[] content() {
        return _content;
    }

    /** Determines whether two blobs are the same.
     * @param b is the blob to compare to
     * @return boolean saying if they equal*/
    Boolean equals(Blob b) {
        return b.hash().equals(_hash) && b.name().equals(_name);
    }
}
