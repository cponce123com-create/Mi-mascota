-- Initial PostgreSQL schema for Mi Mascota
CREATE TYPE "Species" AS ENUM ('CAT', 'DOG', 'OTHER');

CREATE TABLE "User" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "googleSubject" TEXT NOT NULL,
  "displayName" TEXT,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX "User_googleSubject_key" ON "User"("googleSubject");

CREATE TABLE "Pet" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "ownerId" TEXT NOT NULL,
  "name" TEXT NOT NULL,
  "species" "Species" NOT NULL,
  "birthDate" TIMESTAMP(3),
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "Pet_ownerId_fkey" FOREIGN KEY ("ownerId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE INDEX "Pet_ownerId_idx" ON "Pet"("ownerId");

CREATE TABLE "Vaccine" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "petId" TEXT NOT NULL,
  "name" TEXT NOT NULL,
  "administeredAt" TIMESTAMP(3),
  "nextDueAt" TIMESTAMP(3),
  CONSTRAINT "Vaccine_petId_fkey" FOREIGN KEY ("petId") REFERENCES "Pet"("id") ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX "Vaccine_petId_idx" ON "Vaccine"("petId");

CREATE TABLE "Photo" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "petId" TEXT NOT NULL,
  "storageKey" TEXT NOT NULL,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "Photo_petId_fkey" FOREIGN KEY ("petId") REFERENCES "Pet"("id") ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX "Photo_petId_idx" ON "Photo"("petId");

CREATE TABLE "Reminder" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "petId" TEXT NOT NULL,
  "title" TEXT NOT NULL,
  "dueAt" TIMESTAMP(3) NOT NULL,
  "completed" BOOLEAN NOT NULL DEFAULT false,
  CONSTRAINT "Reminder_petId_fkey" FOREIGN KEY ("petId") REFERENCES "Pet"("id") ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX "Reminder_petId_dueAt_idx" ON "Reminder"("petId","dueAt");

CREATE TABLE "LostPetAlert" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "petId" TEXT NOT NULL,
  "status" TEXT NOT NULL DEFAULT 'OPEN',
  "district" TEXT,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "LostPetAlert_petId_fkey" FOREIGN KEY ("petId") REFERENCES "Pet"("id") ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX "LostPetAlert_status_district_idx" ON "LostPetAlert"("status","district");

CREATE TABLE "Post" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "authorId" TEXT NOT NULL,
  "title" TEXT NOT NULL,
  "body" TEXT NOT NULL,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "Post_authorId_fkey" FOREIGN KEY ("authorId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE INDEX "Post_createdAt_idx" ON "Post"("createdAt");
