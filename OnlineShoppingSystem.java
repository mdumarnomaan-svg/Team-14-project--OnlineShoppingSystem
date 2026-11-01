import java.io.*;
import java.util.*;

public class OnlineShoppingSystem {
    static final Scanner sc = new Scanner(System.in);
    static final String PRODUCTS_FILE = "products.txt";
    static final String ORDERS_FILE = "orders.txt";
    static final ArrayList<Product> products = new ArrayList<>();
    static final LinkedHashMap<Integer, Integer> cart = new LinkedHashMap<>();

    static class Product {
        int id, stock;
        String name;
        double price;
        Product(int id, String name, double price, int stock) {
            this.id = id; this.name = name; this.price = price; this.stock = stock;
        }
    }

    static void loadProducts() {
        File file = new File(PRODUCTS_FILE);
        if (!file.exists()) {
            products.add(new Product(1, "Notebook", 60, 20));
            products.add(new Product(2, "Pen Set", 120, 15));
            products.add(new Product(3, "USB Drive", 550, 10));
            products.add(new Product(4, "Mouse", 750, 8));
            saveProducts();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] a = line.split("\\|", -1);
                if (a.length == 4) products.add(new Product(Integer.parseInt(a[0]), a[1], Double.parseDouble(a[2]), Integer.parseInt(a[3])));
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Could not read products: " + e.getMessage());
        }
    }

    static void saveProducts() {
        try (PrintWriter out = new PrintWriter(new FileWriter(PRODUCTS_FILE))) {
            for (Product p : products) out.println(p.id + "|" + p.name + "|" + p.price + "|" + p.stock);
        } catch (IOException e) { System.out.println("Save error: " + e.getMessage()); }
    }

    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(sc.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a valid whole number."); }
        }
    }

    static Product findProduct(int id) {
        for (Product p : products) if (p.id == id) return p;
        return null;
    }

    static void showProducts() {
        System.out.println("\nID   Product              Price (Rs.)   Stock");
        System.out.println("-----------------------------------------------");
        for (Product p : products)
            System.out.printf("%-4d %-20s %-13.2f %d%n", p.id, p.name, p.price, p.stock);
    }

    static void addToCart() {
        showProducts();
        int id = readInt("Enter product ID: ");
        Product p = findProduct(id);
        if (p == null) { System.out.println("Product not found."); return; }
        int qty = readInt("Enter quantity: ");
        if (qty <= 0 || qty + cart.getOrDefault(id, 0) > p.stock) {
            System.out.println("Invalid quantity or insufficient stock."); return;
        }
        cart.put(id, cart.getOrDefault(id, 0) + qty);
        System.out.println("Added to cart!");
    }

    static double showCart() {
        double total = 0;
        System.out.println("\n--- YOUR CART ---");
        if (cart.isEmpty()) System.out.println("Cart is empty.");
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Product p = findProduct(entry.getKey());
            if (p == null) continue;
            double subtotal = p.price * entry.getValue();
            System.out.printf("%s x %d = Rs. %.2f%n", p.name, entry.getValue(), subtotal);
            total += subtotal;
        }
        System.out.printf("Total: Rs. %.2f%n", total);
        return total;
    }

    static void removeFromCart() {
        int id = readInt("Product ID to remove: ");
        if (cart.remove(id) != null) System.out.println("Removed.");
        else System.out.println("Product not in cart.");
    }

    static void checkout() {
        if (cart.isEmpty()) { System.out.println("Cart is empty."); return; }
        double total = showCart();
        System.out.print("Customer name: ");
        String name = sc.nextLine().trim().replace('|', ' ');
        if (name.isEmpty()) { System.out.println("Name is required."); return; }
        for (Map.Entry<Integer, Integer> item : cart.entrySet()) {
            Product p = findProduct(item.getKey());
            if (p == null || item.getValue() > p.stock) {
                System.out.println("Stock changed. Cannot complete order."); return;
            }
        }
        String orderId = "ORD" + System.currentTimeMillis();
        StringBuilder details = new StringBuilder();
        for (Map.Entry<Integer, Integer> item : cart.entrySet()) {
            Product p = findProduct(item.getKey());
            if (details.length() > 0) details.append(", ");
            details.append(p.name).append(" x").append(item.getValue());
        }
        try (PrintWriter out = new PrintWriter(new FileWriter(ORDERS_FILE, true))) {
            out.println(orderId + "|" + name + "|" + details + "|" + String.format(Locale.US, "%.2f", total));
        } catch (IOException e) { System.out.println("Order could not be saved: " + e.getMessage()); return; }
        for (Map.Entry<Integer, Integer> item : cart.entrySet()) findProduct(item.getKey()).stock -= item.getValue();
        saveProducts();
        cart.clear();
        System.out.println("Order placed! Your order ID: " + orderId);
    }

    static void viewOrders() {
        File file = new File(ORDERS_FILE);
        if (!file.exists()) { System.out.println("No orders yet."); return; }
        System.out.println("\n--- ORDER HISTORY ---");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) System.out.println(line.replace('|', ' '));
        } catch (IOException e) { System.out.println("Read error: " + e.getMessage()); }
    }

    public static void main(String[] args) {
        loadProducts();
        while (true) {
            System.out.println("\n===== ONLINE SHOPPING SYSTEM =====");
            System.out.println("1. View Products\n2. Add to Cart\n3. View Cart\n4. Remove from Cart\n5. Checkout\n6. View Orders\n7. Exit");
            int choice = readInt("Choose option: ");
            switch (choice) {
                case 1: showProducts(); break;
                case 2: addToCart(); break;
                case 3: showCart(); break;
                case 4: removeFromCart(); break;
                case 5: checkout(); break;
                case 6: viewOrders(); break;
                case 7: System.out.println("Thank you!"); return;
                default: System.out.println("Invalid option.");
            }
        }
    }
}
