package com.myriam.unrp.player;

/**
 * Entraînement des stats, repris du mod modèle (PointsManager.stepCost / applyStep / refundStat) :
 * - un palier monte le POTENTIEL d'une stat de +0,5 ;
 * - 4 paliers au plus par stat (+2,0 au total) ;
 * - le palier n°n coûte n points de stats (1, 2, 3 puis 4 : 10 points pour une stat à fond) ;
 * - le potentiel ne dépasse jamais 10,0 : un palier est raccourci ou refusé près du plafond ;
 * - retirer un palier rend son coût.
 * Les points de stats sont gagnés comme les points d'aptitude : 1 par niveau (voir ability/Unlocks).
 * Les potentiels sont en dixièmes (65 = 6,5).
 */
public final class Training {
    private Training() {}

    public static final int MAX_STEPS = 4;
    /** gain d'un palier, en dixièmes (+0,5) */
    public static final int STEP = 5;
    /** plafond du potentiel, en dixièmes (10,0) */
    public static final int CEILING = 100;

    /** Coût du prochain palier quand on en a déjà acheté `steps`. */
    public static int cost(int steps) {
        return steps + 1;
    }

    /** Dixièmes réellement gagnés par un palier à partir de ce potentiel (0 = au plafond). */
    public static int gain(int potTenths) {
        return Math.max(0, Math.min(STEP, CEILING - potTenths));
    }

    /** Points déjà dépensés pour ces paliers (1 + 2 + ... + n par stat). */
    public static int spent(int[] steps) {
        int s = 0;
        for (int n : steps) s += n * (n + 1) / 2;
        return s;
    }

    /** Dixièmes à retirer en rendant le dernier palier d'une stat (partial = gain raccourci du dernier palier, 0 si complet). */
    public static int refundTenths(int partial) {
        return partial > 0 ? partial : STEP;
    }

    /** Points rendus en retirant le dernier palier (il avait coûté `steps`). */
    public static int refundPoints(int steps) {
        return steps;
    }
}
