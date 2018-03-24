package belt;

public class SearchOld {
//    Chain chain = new Chain.ChainBuilder().create(Resource.HEATING_PLATE);
//    Field field = Field.emptyField(10, 20);
//    void run(){
//        trySubChainToTarget(5, 0);
//    }
//
//    private boolean trySubChainToTarget(int targetX, int targetY) {
//        Field field = new Field(this.field);
//        field.set(5, 0, new Piece(SELLER));
//
//        // First place everything
//
//        return trySubChainToTarget(field, targetX, targetY, chain);
//    }
//    private boolean tryPlace(Field field, int x, int y, Piece.Orientation o,
//                             Chain chain) {
//        Field copy;
//        if(field.canPlace(x, y)){
//            copy = new Field(field);
//            field.place(x, y, new Piece(chain.getEndBlock(), o, chain.getMaterialOutput()));
//
//        }
//        return false;
//    }
//
//    private boolean incrementLeaf(Chain tree){
//        while(tree.hasNext()){
//            Chain next = tree.peek();
//            if(incrementLeaf(next)){
//                return true;
//            }
//            //tree.nextAndResetAllPrev();
//        }
//        return false;
//    }
//
//    private boolean trySubChainToTarget(Field field, int targetX, int targetY, Chain chain) {
//        // this should increment the deepest value and then return
////        assert !field.canPlace(targetX, targetY);
////        while (chain.hasNext()){
////            Chain child = chain.peek();
////            if(field.canPlace(targetX-1, targetY)){
////                copy = new Field(field);
////                tryPlace(copy, targetX-1, targetY, Field.Orientation.E, child);
////                if(!trySubChainToTarget(copy, targetX-1, targetY, child)) {
////
////                }
////            }
////            if(field.canPlace(targetX+1, targetY)) {
////                copy = new Field(field);
////                place(copy, targetX + 1, targetY, Field.Orientation.W, child);
////                trySubChainToTarget(copy, targetX, targetY,
////                    chain, ++nextChild, nextChildPosition);
////            }
////            if(field.canPlace(targetX, targetY-1)) {
////                copy = new Field(field);
////                place(copy, targetX, targetY-1, Field.Orientation.S, child);
////                trySubChainToTarget(copy, targetX, targetY,
////                    chain, ++nextChild, nextChildPosition);
////            }
////            if(field.canPlace(targetX, targetY+1)) {
////                copy = new Field(field);
////                place(copy, targetX, targetY+1, Field.Orientation.N, child);
////                trySubChainToTarget(copy, targetX, targetY,
////                    chain, ++nextChild, nextChildPosition);
////            }
////        }
////
////
////        Chain[] children = chain.getChildren();
////        if(children.length == nextChild){
////            // Placed all children
////            return true;
////        }
////        Chain child = children[nextChild];
//
//        return false;
//    }
//
//    private Piece.Orientation placePath(Field field, int x, int y, int targetX, int targetY, Chain child) {
//        //place child
//        Piece.PieceType pieceType = child.getEndBlock();
//        Resource.Material material = child.getMaterialOutput();
//        Piece.Orientation orientation = null;
//        field.place(x, y, new Piece(pieceType, orientation, material));
//
//        return null;
//    }
//
//    private boolean findPath(int x, int y, int targetX, int targetY) {
//
//        return false;
//    }
}
