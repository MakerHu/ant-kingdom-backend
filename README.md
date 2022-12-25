# 蚂蚁星球

特别感谢 22126384 宋廷泽同学在本游戏的实现过程中提供了大量的帮助，本项目前端由本人 22126354 胡江浩实现，后端由 22126384 宋廷泽实现，因此本项目中代码的贡献量可认定为胡江浩 50%，宋廷泽 50%。老师可据此为宋廷泽同学加分。

注：  
22126354 胡江浩 GitHub 账号：MakerHu  
22126384 宋廷泽 GitHub 账号：songtingze

前端地址：https://github.com/MakerHu/ant-kingdom-front

## 游戏背景
在一个未被人类踏足的未知星球上，有一种生物在进化道路上选择了一条与众不同的进化方向——超个体智慧。他们每一个个体就像人体中的一个细胞那样弱不禁风，但由这样无数个职责分明的个体组成的大型生命系统，却让他们成为了这个星球上统治级别的存在。他们就是蚂蚁，但与地球上的蚂蚁不同，这儿的不同种蚂蚁间能够达成领土共识，即只要他们在同一片领土上达成共识，就能团结一致，各司其职，誓死守卫共同的领土。但是，达成共识并不是这么容易，每块领土需要有更多的食物来吸引游荡的蚂蚁种群与之达成共识，否则很有可能被更强的领土消灭并掠夺食物。也正是这个原因，这个星球上形成了几大领土，领土与领土之间经常为了食物产生冲突。而你，作为其中一个领土控制系统中的一员，唯一职责就是对局部冲突进行战略指挥。由于领土每天都在各个边界产生大量的冲突，因此，你与其他指挥蚁一样，都将被分配数量相同的食物作为兵蚁战斗时的消耗，当兵蚁被敌方俘虏，则会被收缴其携带的食物，若分配给你的食物被敌方掠夺完，则你此次的指挥失败。领土为了全局考虑，将不再此次冲突中为你补充粮食，因此你只能全身而退等待下次冲突，争取为领土夺得更多的食物。

发生冲突的通常是在一个富饶的地方，这个地方产出的食物将在你取得胜利后补充到你所在领土的粮仓里，但要注意，是在冲突结束后胜方才能得到环境产出的粮食，因此该粮食并不会影响当前冲突双方携带的食物。

在冲突的过程中，我们免不了为了胜利消耗一部分的食物，食物一般消耗在：

1. 与野外零散的蚂蚁种群达成共识，使其加入你的领土为你战斗。
2. 改造环境，不同的兵蚁在不同的环境中的战斗力不同，消耗食物改造环境能让你派出的兵蚁，发挥最大的作用。但如果对方花更多的食物改造环境，则你的环境将被覆盖。
3. 派出的每只兵蚁都需要携带一定的食物作为战时消耗，若回合战斗失败，则派出的兵蚁将战死沙场，其携带的食物都将被敌人夺走。

环境中的食物一般只在冬季之前产出，当冬季到来时，拥有最多粮食储备的领土将度过一个美好的冬天，直到来年重新开始粮食的争夺。由于该星球的公转速度比地球快得多，因此地球上的几天就相当于该星球的一年。从地球的视角，我们能够快速看到该星球上一个个领土的壮大与衰弱。

## 游戏规则
玩家的核心目标是争夺本次冲突发生地环境产生的粮食，以及其他玩家蚂蚁携带的粮食。规则如下：
1. 在每个回合中，首先选择手中两张蚂蚁牌作为暗牌打出，当双方都出牌后，转明牌双方可见；
2. 接着根据场上情况及手牌情况，选择手中两张蚂蚁牌作为暗牌打出，当双方都出牌后，转明牌双方可见；
3. 回合的任意时刻，拥有环境牌的玩家都可以使用米粒改造环境，但是后改环境的玩家需要付出比上次环境被改造所消耗米粒数更多的米粒，无论上次环境是哪位玩家改造。
4. 场上的双方8张牌全部转明牌后，如果所有玩家同意结束回合而不继续改造环境，则回合结束，系统计算赢家及米粒收获，同时玩家可在此时点击野外蚂蚁补充兵力；
5. 如此循环每个回合，一方米粒不足以支撑战斗继续后，胜者获得最初环境产生的所有米粒，并将赢得的米粒补充到领土粮仓中。
6. 在游戏过程中，玩家需要根据场上环境及的牌局情况决定自己的出牌，不同的牌通过不同的组合策略能够发挥不一样的效果。
7. 争取多参与到争夺食物的冲突中，为自己的领土扩充粮仓。

## 数据库
### 结构
```mysql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for card
-- ----------------------------
DROP TABLE IF EXISTS `card`;
CREATE TABLE `card`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `type` int NOT NULL COMMENT '0:蚂蚁牌;1:环境牌',
  `init_value` int NOT NULL,
  `rice` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for card_relation
-- ----------------------------
DROP TABLE IF EXISTS `card_relation`;
CREATE TABLE `card_relation`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `card1` int NOT NULL,
  `card2` int NOT NULL,
  `value_impact` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for room
-- ----------------------------
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room`  (
  `id` varchar(6) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `people_num` int NOT NULL,
  `status` int NOT NULL COMMENT '0:未开始;1:正在进行;3:已结束',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `uid` int NOT NULL AUTO_INCREMENT,
  `uname` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`uid`) USING BTREE,
  UNIQUE INDEX `uname`(`uname`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

```

### 数据
**card表**
```mysql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for card
-- ----------------------------
DROP TABLE IF EXISTS `card`;
CREATE TABLE `card`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `type` int NOT NULL COMMENT '0:蚂蚁牌;1:环境牌',
  `init_value` int NOT NULL,
  `rice` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of card
-- ----------------------------
ALTER TABLE `card` AUTO_INCREMENT=1;
# 环境牌
INSERT INTO `card` VALUES (1, '森林', 1, 0, 0);
INSERT INTO `card` VALUES (2, '草原', 1, 0, 0);
INSERT INTO `card` VALUES (3, '沙漠', 1, 0, 0);

# 蚂蚁牌
INSERT INTO `card` VALUES (4, '工匠收获蚁', 0, 120, 120);
INSERT INTO `card` VALUES (5, '草地铺道蚁', 0, 100, 100);
INSERT INTO `card` VALUES (6, '红火蚁', 0, 150, 150);
INSERT INTO `card` VALUES (7, '血红林蚁', 0, 80, 80);
INSERT INTO `card` VALUES (8, '大黑蚂蚁', 0, 50, 50);
INSERT INTO `card` VALUES (9, '黄猄蚁', 0, 90, 90);
INSERT INTO `card` VALUES (10, '子弹蚁', 0, 200, 200);
INSERT INTO `card` VALUES (11, '巨首芭切叶蚁', 0, 150, 150);
INSERT INTO `card` VALUES (12, '撒哈拉银蚁', 0, 100, 100);
INSERT INTO `card` VALUES (13, '费氏弓背蚁', 0, 130, 130);


SET FOREIGN_KEY_CHECKS = 1;

```

**card_relation**
```mysql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for card_relation
-- ----------------------------
DROP TABLE IF EXISTS `card_relation`;
CREATE TABLE `card_relation`  (
                                 `id` int NOT NULL AUTO_INCREMENT,
                                 `card1` int NOT NULL,
                                 `card2` int NOT NULL,
                                 `value_impact` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of card_relation
-- ----------------------------
ALTER TABLE `card_relation` AUTO_INCREMENT=1;


#工匠收获蚁
INSERT INTO `card_relation` VALUES (null, 4, 1, '+20');
INSERT INTO `card_relation` VALUES (null, 4, 2, '+20');
INSERT INTO `card_relation` VALUES (null, 4, 3, '+20');
INSERT INTO `card_relation` VALUES (null, 4, 4, '+10');
INSERT INTO `card_relation` VALUES (null, 4, 5, '+20');

#草地铺道蚁
INSERT INTO `card_relation` VALUES (null, 5, 1, '+30');
INSERT INTO `card_relation` VALUES (null, 5, 2, '+30');
INSERT INTO `card_relation` VALUES (null, 5, 3, '-60');
INSERT INTO `card_relation` VALUES (null, 5, 5, '+10');
INSERT INTO `card_relation` VALUES (null, 5, 4, '+20');

#红火蚁
INSERT INTO `card_relation` VALUES (null, 6, 1, '-30');
INSERT INTO `card_relation` VALUES (null, 6, 2, '+60');
INSERT INTO `card_relation` VALUES (null, 6, 3, '-30');
INSERT INTO `card_relation` VALUES (null, 6, 6, '+10');

#血红林蚁
INSERT INTO `card_relation` VALUES (null, 7, 1, '+40');
INSERT INTO `card_relation` VALUES (null, 7, 2, '+0');
INSERT INTO `card_relation` VALUES (null, 7, 3, '-20');
INSERT INTO `card_relation` VALUES (null, 7, 7, '+10');
INSERT INTO `card_relation` VALUES (null, 7, 8, '+80');

#大黑蚂蚁
INSERT INTO `card_relation` VALUES (null, 8, 1, '+10');
INSERT INTO `card_relation` VALUES (null, 8, 2, '+0');
INSERT INTO `card_relation` VALUES (null, 8, 3, '+0');
INSERT INTO `card_relation` VALUES (null, 8, 8, '+10');
INSERT INTO `card_relation` VALUES (null, 8, 7, '+80');

#黄猄蚁
INSERT INTO `card_relation` VALUES (null, 9, 1, '+50');
INSERT INTO `card_relation` VALUES (null, 9, 2, '-10');
INSERT INTO `card_relation` VALUES (null, 9, 3, '-40');
INSERT INTO `card_relation` VALUES (null, 9, 9, '+10');

#子弹蚁
INSERT INTO `card_relation` VALUES (null, 10, 1, '+20');
INSERT INTO `card_relation` VALUES (null, 10, 2, '+0');
INSERT INTO `card_relation` VALUES (null, 10, 3, '-50');
INSERT INTO `card_relation` VALUES (null, 10, 10, '+10');

#巨首芭切叶蚁
INSERT INTO `card_relation` VALUES (null, 11, 1, '+50');
INSERT INTO `card_relation` VALUES (null, 11, 2, '+5');
INSERT INTO `card_relation` VALUES (null, 11, 3, '-60');
INSERT INTO `card_relation` VALUES (null, 11, 11, '+10');

#撒哈拉银蚁
INSERT INTO `card_relation` VALUES (null, 12, 1, '-30');
INSERT INTO `card_relation` VALUES (null, 12, 2, '-20');
INSERT INTO `card_relation` VALUES (null, 12, 3, '+100');
INSERT INTO `card_relation` VALUES (null, 12, 12, '+10');

#费氏弓背蚁
INSERT INTO `card_relation` VALUES (null, 13, 1, '-10');
INSERT INTO `card_relation` VALUES (null, 13, 2, '+10');
INSERT INTO `card_relation` VALUES (null, 13, 3, '+40');
INSERT INTO `card_relation` VALUES (null, 13, 13, '+10');



SET FOREIGN_KEY_CHECKS = 1;

```

## 系统环境变量配置
在`/etc/profile`中添加下列变量，数据库账号密码根据部署情况调整
```shell
export AK_DBSERVER=127.0.0.1
export AK_DBPORT=3306
export AK_DBNAME=ant_kingdom
export AK_DBUSER=root
export AK_DBPASSOWRD=123456
```
保存退出后`source /etc/profile`让配置生效
