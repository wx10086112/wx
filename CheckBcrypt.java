import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class CheckBcrypt {
  public static void main(String[] args) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String hash = "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2";
    System.out.println("123456=" + encoder.matches("123456", hash));
    System.out.println("admin123=" + encoder.matches("admin123", hash));
    System.out.println("fruit_admin=" + encoder.matches("fruit_admin", hash));
  }
}
