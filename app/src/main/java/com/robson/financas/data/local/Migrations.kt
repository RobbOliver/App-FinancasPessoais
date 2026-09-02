package com.robson.financas.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adiciona `product_aliases` (cache de normalização de nome de produto) sem apagar dados existentes. */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `product_aliases` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `rawDescription` TEXT NOT NULL,
                `canonicalName` TEXT NOT NULL,
                `brand` TEXT,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_product_aliases_rawDescription` ON `product_aliases` (`rawDescription`)",
        )
    }
}

/**
 * Refaz `goals`: deixa de ser 1 meta = 1 categoria (PK composta `yearMonth+categoryId`) e vira
 * uma meta com nome próprio que pode somar várias categorias, via `goal_categories`. Seguro
 * recriar do zero — a tabela `goals` não tem nenhuma linha até esta versão.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `goals`")
        db.execSQL(
            """
            CREATE TABLE `goals` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `yearMonth` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `amountCents` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `goal_categories` (
                `goalId` INTEGER NOT NULL,
                `categoryId` INTEGER NOT NULL,
                PRIMARY KEY(`goalId`, `categoryId`),
                FOREIGN KEY(`goalId`) REFERENCES `goals`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_goal_categories_goalId` ON `goal_categories` (`goalId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_goal_categories_categoryId` ON `goal_categories` (`categoryId`)")
    }
}

/**
 * Cada categoria de uma meta ganha sua própria fatia do valor total (`allocatedCents`), pra
 * conseguir marcar em vermelho quando o gasto daquela categoria específica estoura sua fatia.
 * Metas já existentes ficam com `0` em todas as categorias até serem editadas novamente.
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `goal_categories` ADD COLUMN `allocatedCents` INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Duas mudanças: (1) `categories.isAiTaxonomy` marca o que é gerido pela taxonomia fiscal, pra
 * separar a aba "IA" (não excluível) das abas "Receitas"/"Despesas" e (2) as FKs de
 * `user_classification_rules` para categoria/subcategoria/microcategoria trocam de RESTRICT pra
 * CASCADE — hoje uma regra de classificação (criada silenciosamente ao corrigir um item na tela
 * de revisão) trava para sempre a exclusão da categoria correspondente, sem nenhuma tela pra
 * gerenciar essas regras. SQLite não altera FK de tabela existente, então recriamos a tabela
 * preservando as linhas.
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `categories` ADD COLUMN `isAiTaxonomy` INTEGER NOT NULL DEFAULT 0")

        db.execSQL(
            """
            CREATE TABLE `user_classification_rules_new` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `matchType` TEXT NOT NULL,
                `matchValue` TEXT NOT NULL,
                `productId` INTEGER,
                `categoryId` INTEGER NOT NULL,
                `subcategoryId` INTEGER,
                `microcategoryId` INTEGER,
                `priority` INTEGER NOT NULL,
                `active` INTEGER NOT NULL,
                `timesApplied` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON DELETE SET NULL,
                FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`subcategoryId`) REFERENCES `categories`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`microcategoryId`) REFERENCES `microcategories`(`id`) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `user_classification_rules_new`
            SELECT `id`, `matchType`, `matchValue`, `productId`, `categoryId`, `subcategoryId`, `microcategoryId`,
                   `priority`, `active`, `timesApplied`, `createdAt`
            FROM `user_classification_rules`
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `user_classification_rules`")
        db.execSQL("ALTER TABLE `user_classification_rules_new` RENAME TO `user_classification_rules`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_classification_rules_productId` ON `user_classification_rules` (`productId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_classification_rules_categoryId` ON `user_classification_rules` (`categoryId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_classification_rules_subcategoryId` ON `user_classification_rules` (`subcategoryId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_classification_rules_microcategoryId` ON `user_classification_rules` (`microcategoryId`)")
    }
}

/** Controla, por conta, se ela aparece no card de saldo por conta do Resumo — sem afetar o saldo total. */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `accounts` ADD COLUMN `showOnDashboard` INTEGER NOT NULL DEFAULT 1")
    }
}
