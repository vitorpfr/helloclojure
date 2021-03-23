import pirate_phrases.*;

public class PirateConversation {
    public static void main(String[] args) {
        Greetings greetings = new Greetings();
        greetings.hello();

        Farewells farewells = new Farewells();
        farewells.goodbye();
    }
}

// Build a jar:
// javac PirateConversation.java
// jar cvfe conversation.jar PirateConversation PirateConversation.class pirate_phrases/*.class

// See jar content
// jar tf conversation.jar

// Run jar
// java -jar conversation.jar
