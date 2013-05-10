package com.svamp.planetwars.math;

import android.graphics.Rect;
import android.graphics.RectF;
import com.svamp.planetwars.sprite.Sprite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * Implementation of quadTree from wikipedia for fast spatial search.
 * All data points on leaf nodes. Log(n) removal of points.
 */
public class QuadTree<T extends Sprite> implements Collection<T> {
    private int size=0;
    private static final int QT_NODE_CAPACITY = 4;
    private RectF boundary = new RectF(0,0,0,0);

    //Parent
    private final QuadTree<T> parent;

    //Points in quadtree node
    private final List<T> points = new ArrayList<T>();

    //Child nodes.
    private QuadTree<T> northWest;
    private QuadTree<T> northEast;
    private QuadTree<T> southWest;
    private QuadTree<T> southEast;

    public QuadTree(RectF boundary,QuadTree<T> parent) {
        this.parent=parent;
        this.boundary = boundary;
    }

    @Override
    public boolean add(T p) {
        // Ignore objects which do not belong in this quad tree
        if (!boundary.contains(p.getBounds().centerX(), p.getBounds().centerY())) {
            return false; // object cannot be added
        }
        size++;

        // If there is space in this quad tree, add the object here
        if (points.size() < QT_NODE_CAPACITY && northWest==null) {
            points.add(p);
            return true;
        }

        // Otherwise, we need to subdivide then add the point to whichever node will accept it
        if (northWest == null)
            subdivide();

        //We CANNOT have points and children at the same time! Add all points to children.
        northWest.addAll(points);
        northEast.addAll(points);
        southWest.addAll(points);
        southEast.addAll(points);
        this.points.clear(); //Delete internal  point db.

        if (northWest.add(p)) return true;
        if (northEast.add(p)) return true;
        if (southWest.add(p)) return true;
        if (southEast.add(p)) return true;

        // Otherwise, the point cannot be inserted for some unknown reason (which should never happen)
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        for(T t : collection) {
            this.add(t);
        }
        return true;
    }

    @Override
    public void clear() {
        this.northWest=null;
        this.northEast=null;
        this.southWest=null;
        this.southEast=null;
        this.points.clear();
    }

    @Override
    public boolean contains(Object object) {
        //Leaf node. Object contained within?
        if(northWest==null) return points.contains(object);
        //Check if any of the child nodes contain the object...
        return northWest.contains(object)
                || northEast.contains(object)
                || southEast.contains(object)
                || southWest.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for(Object e : collection) {
            if(!this.contains(e))
                return false; //One element not found. Return false.
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return points.size()==0 && northWest==null;
    }

    @Override
    public Iterator<T> iterator() {
        return new QuadTreeIterator(this);
    }

    @Override
    public boolean remove(Object object) {
        boolean isRemoved;
        if(!(object instanceof QuadTree)) return false;
        //Send signal down the tree
        if(northEast!=null) {
            isRemoved =
                    northEast.remove(object)
                            ||northWest.remove(object)
                            ||southEast.remove(object)
                            ||southWest.remove(object);
        } else { //Leaf node.
            isRemoved = points.remove(object);
            if(isRemoved) decreaseSize();
            return isRemoved;
        }
        //Lastly, clean up. We might have only empty children now..
        if(northEast.isEmpty() && northWest.isEmpty() && southWest.isEmpty() && southEast.isEmpty()) {
            northEast=null;
            northWest=null;
            southEast=null;
            southWest=null;
        }
        return isRemoved;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        boolean success = false;
        for(Object s : collection) {
            if(remove(s)) success=true;
        }
        return success;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;  //TODO: Implement
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Sprite[size];
        int i=0;
        for(T t : this) {
            array[i]= t;
            i++;
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] array) {
        //Types fail miserably. Return garbage. TODO: fix.
        return (T[]) toArray();
    }

    /**
     * Find all Sprites contained within the given bounding box
     * @param range RectF containing all sprites of interest
     * @return List of sprites.
     */
    List<T> queryRange(RectF range)
    {
        // Prepare an array of results
        List<T> pointsInRange = new ArrayList<T>();

        // Automatically abort if the range does not collide with this quad
        if (!boundary.intersects(range.left,range.top,range.right,range.bottom))
            return pointsInRange; // empty list

        // Check objects at this quad level
        for (T p : points) {
            RectF b = p.getBounds();
            if (range.intersects(b.left, b.top, b.right, b.bottom))
                pointsInRange.add(p);
        }

        // Terminate here, if there are no children
        if (northWest == null)
            return pointsInRange;

        // Otherwise, add the points from the children
        pointsInRange.addAll(northWest.queryRange(range));
        pointsInRange.addAll(northEast.queryRange(range));
        pointsInRange.addAll(southWest.queryRange(range));
        pointsInRange.addAll(southEast.queryRange(range));
        return pointsInRange;
    }


    /**
     * Gets the closest point in the quadTree to the given vector coordinate.
     * The search is bounded by maxDistance provided.
     * This maxDistance is a box, not a circle around the vector.
     * @param p Center point.
     * @param maxDist Max distance
     * @return Closest point to the vector, or null if the vector point is alone in the box.
     */
    public T getClosest(Vector p,float maxDist) {
        List<T> neighbors = queryRange(new RectF(p.x-maxDist,p.y-maxDist,p.x+maxDist,p.y+maxDist));

        T closest=null;
        double closestDist = Double.MAX_VALUE;
        for(T n : neighbors) {
            double dist = Math.abs(n.getBounds().centerX()-p.x)+Math.abs(n.getBounds().centerY()-p.y);
            if(dist<closestDist) {
                closest=n;
                closestDist=dist;
            }
        }
        return closest;
    }


    /**
     * Makes QuadTree nodes for this one.
     */
    private void subdivide() {
        northWest = new QuadTree<T>(new RectF(
                boundary.left,
                boundary.top,
                boundary.centerX(),
                boundary.centerY()),
                this);
        northEast = new QuadTree<T>(new RectF(
                boundary.centerX(),
                boundary.top,
                boundary.right,
                boundary.centerY()),
                this);
        southWest = new QuadTree<T>(new RectF(
                boundary.left,
                boundary.centerY(),
                boundary.centerX(),
                boundary.bottom),
                this);
        southEast = new QuadTree<T>(new RectF(
                boundary.centerX(),
                boundary.centerY(),
                boundary.right,
                boundary.bottom),
                this);
    }

    private void decreaseSize() {
        size--;
        if(parent!=null)
            parent.decreaseSize();
    }

    private class QuadTreeIterator implements Iterator<T> {
        private final QuadTree<T> root;
        private final Stack<QuadTree<T>> unexplored = new Stack<QuadTree<T>>();
        private final Stack<T> foundPoints = new Stack<T>();
        private T last;

        private QuadTreeIterator(QuadTree<T> tree) {
            root = tree;
            unexplored.push(tree); //Push the tree to iterate over to the unexplored stack
        }

        @Override
        public boolean hasNext() {
            //Explore the tree until we find points
            while(foundPoints.empty() && !unexplored.empty()) {
                //Get a node to explore.

                QuadTree<T> searchNode = unexplored.pop();

                //Does it contain points? If so, add them.
                if(!searchNode.points.isEmpty()) foundPoints.addAll(searchNode.points);
                //Add children of search node for further search
                if(searchNode.northWest!=null) {
                    unexplored.push(searchNode.northWest);
                    unexplored.push(searchNode.northEast);
                    unexplored.push(searchNode.southWest);
                    unexplored.push(searchNode.southEast);
                }
            }
            //If we found points, We have a next element.
            return !foundPoints.empty();
        }

        @Override
        public T next() {
            if(!hasNext())
                //Foundpoints is still empty after searching tree! Throw exception
                throw new NoSuchElementException("Reached end of QuadTree!");

            //Return a point found.
            last = foundPoints.pop();
            return last;


        }

        @Override
        public void remove() {
            root.remove(last); //Inefficient, but oh well..
        }
    }

    public String toString() {
        if(!points.isEmpty()) return points.toString();
        if(northWest!=null)
            return "\n    [QuadTree \n"+northWest.toString()+"\n"+northEast.toString()+"\n"+southWest.toString()+"\n"+southEast.toString()+"]";
        return "null";
    }
}
