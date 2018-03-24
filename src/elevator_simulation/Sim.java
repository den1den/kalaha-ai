package elevator_simulation;

import java.util.Random;

public class Sim {
    int speed; // seconds per floor
    int floors;
    float spawnrate; // people per second
    int elevators;

    int simulate(int time, long seed) {
        int[][] waiting = new int[floors][];
        for (int i = 0; i < floors; i++) {
            waiting[i] = new int[floors];
        }

        int[] ePosition = new int[this.elevators];
        int[] eDirection = new int[this.elevators];
        int[][] eLoad = new int[this.elevators][];
        for (int i = 0; i < this.elevators; i++) {
            eLoad[i] = new int[floors];
        }

        int brought = 0;
        Random random = new Random(seed);
        while (time-- > 0) {
            waiting[random.nextInt(floors)][random.nextInt(floors)]++;

            for (int e = 0; e < eDirection.length; e++) {
                if (eDirection[e] == speed) {
                    // arrived
                    int atFloor = ePosition[e]++;
                    // empty it
                    brought += eLoad[e][atFloor];
                    eLoad[e][atFloor] = 0;
                    // check where to next
                    checkNext:
                    {
                        for (int i = atFloor + 1; i < floors; i++) {
                            if (eLoad[e][i] > 0) {
                                // Found
                                eDirection[e] = 1;
                                break checkNext;
                            }
                        }
                        for (int i = atFloor - 1; i < 0; i++) {

                        }
                    }
                    eDirection[e] = 0;
                }
            }
        }
        return brought;
    }
}
