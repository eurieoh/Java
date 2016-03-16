package gitlet;
import java.io.Serializable;

/** Branch class keeps track of the branches.
 * @author Eurie Oh and Caroline Kim */
class Branch implements Serializable {
    /** The name of the branch. */
    private String _name;
    /** The head of the branch. */
    private String _head;
    /** Initializes branch and sets name and head.
    * @param name the name of the branch
    * @param head the hashcode of the commit.
    */
    Branch(String name, String head) {
        _name = name;
        _head = head;
    }
    /** Method of getting the name.
    * @return name returns the string name. */
    public String name() {
        return _name;
    }
    /** Method of getting the head.
    * @return head returns the hashcode of the commit. */
    public String head() {
        return _head;
    }
    /** Changes the head of the branch to the hash of commits c.
    * @param c takes in the commit that we want to change to.
    */
    public void change(Commits c) {
        _head = c.hash();
    }
}
