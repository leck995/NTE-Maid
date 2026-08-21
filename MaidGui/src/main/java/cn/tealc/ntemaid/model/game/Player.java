package cn.tealc.ntemaid.model.game;

public class Player {
    private long id;
    private String name;
    private boolean isOwner;//是否是自己账号

    public Player(long id, String name,boolean isOwner) {
        this.id = id;
        this.name = name;
        this.isOwner = isOwner;
    }

    public Player() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Player a){
            return a.id == this.id;
        }
        return super.equals(obj);
    }
}
