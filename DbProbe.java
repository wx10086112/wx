import java.sql.*;

public class DbProbe {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/ruoyi-cs?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=utf8";
        String user = "root";
        String pass = "123456";
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            String[] queries = new String[] {
                "select 'merchant' as k, count(*) as v from merchant",
                "select 'distributor' as k, count(*) as v from distributor",
                "select 'merchant_store' as k, count(*) as v from merchant_store",
                "select 'product' as k, count(*) as v from product",
                "select 'groupon_activity' as k, count(*) as v from groupon_activity",
                "select 'mall_order' as k, count(*) as v from mall_order",
                "select 'merchant_user' as k, count(*) as v from merchant_user",
                "select 'payment_record' as k, count(*) as v from payment_record"
            };
            for (String q : queries) {
                try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(q)) {
                    while (rs.next()) {
                        System.out.println(rs.getString(1) + "=" + rs.getString(2));
                    }
                }
            }
            System.out.println("-- merchants --");
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(
                "select id,name,distributor_id,phone,address,c_mini_app_id,m_mini_app_id from merchant order by id limit 50")) {
                while (rs.next()) {
                    System.out.println(String.format("merchant|%s|%s|dist=%s|phone=%s|addr=%s|cApp=%s|mApp=%s",
                        rs.getLong("id"), rs.getString("name"), rs.getString("distributor_id"), rs.getString("phone"),
                        rs.getString("address"), rs.getString("c_mini_app_id"), rs.getString("m_mini_app_id")));
                }
            }
            System.out.println("-- distributors --");
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(
                "select id,name,region_name,status from distributor order by id limit 50")) {
                while (rs.next()) {
                    System.out.println(String.format("distributor|%s|%s|region=%s|status=%s",
                        rs.getLong("id"), rs.getString("name"), rs.getString("region_name"), rs.getString("status")));
                }
            }
            System.out.println("-- merchant summaries --");
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(
                "select m.id,m.name,count(distinct s.id) as store_count,count(distinct p.id) as product_count,count(distinct g.id) as groupon_count from merchant m left join merchant_store s on s.merchant_id=m.id and s.del_flag='0' left join product p on p.merchant_id=m.id and p.del_flag='0' left join groupon_activity g on g.merchant_id=m.id and g.del_flag='0' group by m.id,m.name order by m.id")) {
                while (rs.next()) {
                    System.out.println(String.format("summary|%s|%s|store=%s|product=%s|groupon=%s",
                        rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
                }
            }
        }
    }
}
