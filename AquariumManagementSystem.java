import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

// Interface
interface Maintainable {
    void clean();
    void feed();
}

// Superclass
class Person {
    protected String name;
    protected String contact;

    public Person(String name, String contact) {
        this.name = name;
        this.contact = contact;
    }
}

// Subclass
class Visitor extends Person {
    private int ticketCount;
    private LocalDate visitDate;

    public Visitor(String name, String contact, int ticketCount, LocalDate visitDate) {
        super(name, contact);
        this.ticketCount = ticketCount;
        this.visitDate = visitDate;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }
}

// AquaticAnimal class implementing interface
class AquaticAnimal implements Maintainable {
    private int id;
    private String name;
    private String habitat;
    private double size;
    private int age;
    private String zone;

    public AquaticAnimal(String name, String habitat, double size, int age, String zone) {
        this.name = capitalize(name);
        this.habitat = habitat;
        this.size = size;
        this.age = age;
        this.zone = zone;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getHabitat() { return habitat; }
    public double getSize() { return size; }
    public int getAge() { return age; }
    public String getZone() { return zone; }

    public void setId(int id) { this.id = id; }
    public void setSize(double size) { this.size = size; }
    public void setAge(int age) { this.age = age; }

    @Override
    public void clean() {
        System.out.println("Cleaning the habitat of " + name);
    }

    @Override
    public void feed() {
        System.out.println("Feeding " + name);
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}

public class AquariumManagementSystem {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/aquarium";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "nailah2425";

    // Using ArrayList for storing animals and visitors temporarily
    private static List<AquaticAnimal> animals = new ArrayList<>();
    private static List<Visitor> visitors = new ArrayList<>();
    // HashMap to store maintenance schedule by zone
    private static Map<String, List<String>> maintenanceSchedule = new HashMap<>();

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            setupDatabase(connection);
            run(connection);
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    private static void setupDatabase(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS aquatic_animals (id SERIAL PRIMARY KEY, name VARCHAR(50), habitat VARCHAR(50), size DOUBLE PRECISION, age INT, zone VARCHAR(50))");
            statement.execute("CREATE TABLE IF NOT EXISTS visitors (id SERIAL PRIMARY KEY, name VARCHAR(50), contact VARCHAR(50), ticket_count INT, visit_date DATE)");
            System.out.println("Database setup complete.");
        }
    }

    private static void run(Connection connection) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n1. Add Animal\n2. View Animals\n3. Update Animal\n4. Delete Animal\n5. Add Visitor\n6. View Visitors\n7. Calculate Average Size of Animals\n8. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            try {
                switch (choice) {
                    case 1:
                        addAnimal(connection, scanner);
                        break;
                    case 2:
                        viewAnimals(connection);
                        break;
                    case 3:
                        updateAnimal(connection, scanner);
                        break;
                    case 4:
                        deleteAnimal(connection, scanner);
                        break;
                    case 5:
                        addVisitor(connection, scanner);
                        break;
                    case 6:
                        viewVisitors(connection);
                        break;
                    case 7:
                        calculateAverageSize();
                        break;
                    case 8:
                        System.out.println("Exiting program. Goodbye!");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private static void addAnimal(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Habitat: ");
        String habitat = scanner.nextLine();
        System.out.print("Size (cm): ");
        double size = scanner.nextDouble();
        System.out.print("Age: ");
        int age = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Zone: ");
        String zone = scanner.nextLine();

        AquaticAnimal animal = new AquaticAnimal(name, habitat, size, age, zone);
        animals.add(animal);  // Adding to ArrayList

        // Inserting into the database
        String sql = "INSERT INTO aquatic_animals (name, habitat, size, age, zone) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, habitat);
            statement.setDouble(3, size);
            statement.setInt(4, age);
            statement.setString(5, zone);
            statement.executeUpdate();
        }
        System.out.println("Animal added successfully.");
    }

    private static void viewAnimals(Connection connection) throws SQLException {
        String sql = "SELECT * FROM aquatic_animals";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            System.out.println("\nAnimals in the Aquarium:");
            while (resultSet.next()) {
                System.out.printf("ID: %d, Name: %s, Habitat: %s, Size: %.2f cm, Age: %d, Zone: %s\n",
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("habitat"),
                        resultSet.getDouble("size"),
                        resultSet.getInt("age"),
                        resultSet.getString("zone"));
            }
        }
    }

    private static void updateAnimal(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter the ID of the animal to update: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("New size (cm): ");
        double size = scanner.nextDouble();
        System.out.print("New age: ");
        int age = scanner.nextInt();

        String sql = "UPDATE aquatic_animals SET size = ?, age = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, size);
            statement.setInt(2, age);
            statement.setInt(3, id);
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Animal updated successfully.");
            } else {
                System.out.println("Animal not found.");
            }
        }
    }

    private static void deleteAnimal(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter the ID of the animal to delete: ");
        int id = scanner.nextInt();

        String sql = "DELETE FROM aquatic_animals WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Animal deleted successfully.");
            } else {
                System.out.println("Animal not found.");
            }
        }
    }

    private static void addVisitor(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Contact: ");
        String contact = scanner.nextLine();
        System.out.print("Number of tickets: ");
        int ticketCount = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Visit date (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine();
        LocalDate visitDate = LocalDate.parse(dateInput, DateTimeFormatter.ISO_DATE);

        Visitor visitor = new Visitor(name, contact, ticketCount, visitDate);
        visitors.add(visitor);  // Adding to ArrayList

        String sql = "INSERT INTO visitors (name, contact, ticket_count, visit_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, contact);
            statement.setInt(3, ticketCount);
            statement.setDate(4, Date.valueOf(visitDate));
            statement.executeUpdate();
        }
        System.out.println("Visitor added successfully.");
    }

    private static void viewVisitors(Connection connection) throws SQLException {
        String sql = "SELECT * FROM visitors";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            System.out.println("\nVisitors to the Aquarium:");
            while (resultSet.next()) {
                System.out.printf("ID: %d, Name: %s, Contact: %s, Tickets: %d, Visit Date: %s\n",
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("contact"),
                        resultSet.getInt("ticket_count"),
                        resultSet.getDate("visit_date"));
            }
        }
    }

    private static void calculateAverageSize() {
        double totalSize = 0;
        int count = 0;

        for (AquaticAnimal animal : animals) {
            totalSize += animal.getSize();
            count++;
        }

        if (count > 0) {
            double averageSize = totalSize / count;
            System.out.println("The average size of animals is: " + averageSize + " cm");
        } else {
            System.out.println("No animals available to calculate average size.");
        }
    }
}

