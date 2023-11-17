

public class LabyrintheConsts {

  // generation de nos labyrinthes

  public static final Case[][] LABYRINTHE_1 = new Case[][] {
      { new Mur(0, 0), new Libre(0, 1), new Mur(0, 2), new Mur(0, 3), new Mur(0, 4), new Mur(0, 5), new Mur(0, 6) },
      { new Mur(1, 0), new Libre(1, 1), new Mur(1, 2), new Libre(1, 3), new Libre(1, 4), new Libre(1, 5),
          new Libre(1, 6) },
      { new Mur(2, 0), new Libre(2, 1), new Mur(2, 2), new Mur(2, 3), new Libre(2, 4), new Mur(2, 5), new Mur(2, 6) },
      { new Libre(3, 0), new Libre(3, 1), new Mur(3, 2), new Libre(3, 3), new Libre(3, 4), new Mur(3, 5),
          new Mur(3, 6) },
      { new Mur(4, 0), new Libre(4, 1), new Libre(4, 2), new Libre(4, 3), new Libre(4, 4), new Libre(4, 5),
          new Mur(4, 6) },
      { new Mur(5, 0), new Mur(5, 1), new Mur(5, 2), new Mur(5, 3), new Mur(5, 4), new Libre(5, 5), new Mur(5, 6) },

  };

  public static final Case[][] LABYRINTHE_2 = new Case[][] {
      { new Mur(0, 0), new Libre(0, 1), new Mur(0, 2), new Mur(0, 3), new Mur(0, 4), new Mur(0, 5), new Mur(0, 6) },
      { new Mur(1, 0), new Libre(1, 1), new Mur(1, 2), new Libre(1, 3), new Libre(1, 4), new Libre(1, 5),
          new Libre(1, 6) },
      { new Mur(2, 0), new Libre(2, 1), new Mur(2, 2), new Mur(2, 3), new Libre(2, 4), new Mur(2, 5), new Mur(2, 6) },
      { new Libre(3, 0), new Libre(3, 1), new Mur(3, 2), new Libre(3, 3), new Libre(3, 4), new Mur(3, 5),
          new Mur(3, 6) },
      { new Mur(4, 0), new Libre(4, 1), new Libre(4, 2), new Libre(4, 3), new Libre(4, 4), new Libre(4, 5),
          new Mur(4, 6) },
      { new Mur(5, 0), new Libre(5, 1), new Mur(5, 2), new Mur(5, 3), new Mur(5, 4), new Libre(5, 5), new Mur(5, 6) },
      { new Mur(6, 0), new Mur(6, 1), new Mur(6, 2), new Mur(6, 3), new Mur(6, 4), new Libre(6, 5), new Mur(6, 6) },

  };
  public static final Case[][] LABYRINTHE_3 = new Case[][] {
      { new Mur(0, 0), new Mur(0, 1), new Mur(0, 2), new Mur(0, 3), new Mur(0, 4),new Mur(0, 5), new Mur(0, 6), new Mur(0, 7), new Mur(0, 8), new Mur(0, 9)  },
      { new Mur(1, 0), new Libre(1, 1), new Mur(1, 2), new Libre(1, 3), new Libre(1, 4),new Libre(1, 5), new Libre(1, 6), new Mur(1, 7), new Mur(1, 8), new Mur(1, 9) },
      { new Mur(2, 0), new Libre(2, 1), new Libre(2, 2), new Libre(2, 3), new Mur(2, 4), new Mur(2, 5), new Libre(2, 6), new Mur(2, 7), new Libre(2, 8), new Mur(2, 9)},
      { new Libre(3, 0), new Libre(3, 1), new Mur(3, 2), new Libre(3, 3), new Mur(3, 4),new Libre(3, 5), new Libre(3, 6), new Libre(3, 7), new Libre(3, 8), new Mur(3, 9) },
      { new Libre(4, 0), new Mur(4, 1), new Mur(4, 2), new Libre(4, 3), new Mur(4, 4),new Libre(4, 5), new Libre(4, 6), new Libre(4, 7), new Libre(4, 8), new Mur(4, 9) },
      { new Libre(5, 0), new Mur(5, 1), new Mur(5, 2), new Libre(5, 3), new Mur(5, 4),new Libre(5, 5), new Mur(5, 6), new Libre(5, 7), new Libre(5, 8), new Mur(5, 9)  },
      { new Libre(6, 0), new Libre(6, 1), new Libre(6, 2), new Libre(6, 3), new Mur(6, 4),new Libre(6, 5), new Libre(6, 6), new Libre(6, 7), new Libre(6, 8), new Mur(6, 9)  },
      { new Mur(7, 0), new Libre(7, 1), new Libre(7, 2), new Libre(7, 3), new Mur(7, 4),new Mur(7, 5), new Libre(7, 6), new Libre(7, 7), new Libre(7, 8), new Mur(7, 9)  },
      { new Mur(8, 0), new Libre(8, 1), new Mur(8, 2), new Libre(8, 3), new Mur(8, 4),new Mur(8, 5), new Libre(8, 6), new Mur(8, 7), new Libre(8, 8), new Mur(8, 9) },
      { new Mur(9, 0), new Libre(9, 1), new Mur(9, 2), new Libre(9, 3), new Mur(9, 4),new Libre(9, 5), new Libre(9, 6), new Mur(9, 7), new Libre(9, 8), new Mur(9, 9) },
      { new Libre(10, 0), new Libre(10, 1), new Mur(10, 2), new Libre(10, 3), new Libre(10, 4),new Libre(10, 5), new Libre(10, 6), new Mur(10, 7), new Libre(10, 8), new Libre(10, 9) },
      { new Libre(11, 0), new Mur(11, 1), new Mur(11, 2), new Libre(11, 3), new Libre(11, 4), new Libre(11, 5), new Mur(11, 6), new Mur(11, 7), new Libre(11, 8), new Libre(11, 9) },
      { new Libre(12, 0), new Libre(12, 1), new Libre(12, 2), new Libre(12, 3), new Mur(12, 4),new Libre(12, 5), new Libre(12, 6), new Libre(12, 7), new Libre(12, 8), new Mur(12, 9)  },
      { new Mur(13, 0), new Libre(13, 1), new Libre(13, 2), new Libre(13, 3), new Mur(13, 4), new Mur(13, 5), new Libre(13, 6), new Libre(13, 7), new Libre(13, 8), new Mur(13, 9) },
      { new Mur(14, 0), new Libre(14, 1), new Mur(14, 2), new Libre(14, 3), new Mur(14, 4), new Mur(14, 5), new Libre(14, 6), new Mur(14, 7), new Libre(14, 8), new Mur(14, 9)  },
      { new Mur(15, 0), new Libre(15, 1), new Mur(15, 2), new Libre(15, 3), new Mur(15, 4),new Mur(15, 5), new Libre(15, 6), new Mur(15, 7), new Libre(15, 8), new Mur(15, 9)  },
      { new Libre(16, 0), new Libre(16, 1), new Libre(16, 2), new Libre(16, 3), new Mur(16, 4), new Libre(16, 5), new Libre(16, 6), new Libre(16, 7), new Libre(16,8), new Mur(16, 9)},
      { new Libre(17, 0), new Mur(17, 1), new Mur(17, 2), new Libre(17, 3), new Mur(17, 4), new Libre(17, 5), new Mur(17, 6), new Mur(17, 7), new Libre(17, 8), new Mur(17, 9) },
      { new Libre(18, 0), new Libre(18, 1), new Libre(18, 2), new Libre(18, 3), new Mur(18, 4), new Libre(18, 5), new Libre(18, 6), new Libre(18, 7), new Libre(18, 8), new Mur(18, 9) },
      { new Mur(19, 0), new Libre(19, 1), new Libre(19, 2), new Mur(19, 3), new Mur(19, 4), new Mur(19, 5), new Libre(19, 6), new Libre(19, 7), new Mur(19, 8), new Mur(19, 9)  },
     
      
      

  };

}