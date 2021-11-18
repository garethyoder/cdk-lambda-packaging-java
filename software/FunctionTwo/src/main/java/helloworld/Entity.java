package helloworld;

public class Entity {

    public static final String TABLE_NAME = "entity-table-dev";

    private String pk;
    private String sk;

    private String name;

    public Entity(String pk, String sk, String name) {
        this.pk = pk;
        this.sk = sk;
        this.name = name;
    }


    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }


    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
