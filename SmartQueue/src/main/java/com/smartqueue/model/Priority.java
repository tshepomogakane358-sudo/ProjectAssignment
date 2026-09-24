package com.smartqueue.model;

public class Priority {

    private int    priorityId;
    private String category;
    private int    rankOrder;

    public Priority() {}

    public Priority(String category, int rankOrder) {
        this.category  = category;
        this.rankOrder = rankOrder;
    }

    public int    getPriorityId() { return priorityId; }
    public String getCategory()   { return category; }
    public int    getRankOrder()  { return rankOrder; }

    public void setPriorityId(int priorityId) { this.priorityId = priorityId; }
    public void setCategory(String category)   { this.category   = category; }
    public void setRankOrder(int rankOrder)    { this.rankOrder  = rankOrder; }

    @Override
    public String toString() {
        return "Priority [" + rankOrder + " - " + category + "]";
    }
}
