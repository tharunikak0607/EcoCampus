public abstract class CampusEntity {

    private String id;
    private String name;

    public CampusEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public abstract void displayDetails();
}
