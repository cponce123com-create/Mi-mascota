-- Community moderation and adoption workflow
ALTER TABLE "Post" ADD COLUMN "status" TEXT NOT NULL DEFAULT 'PENDING';
ALTER TABLE "LostPetAlert" ADD COLUMN "visibility" TEXT NOT NULL DEFAULT 'PENDING';
ALTER TABLE "LostPetAlert" ADD COLUMN "details" TEXT;

CREATE TABLE "AdoptionListing" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "authorId" TEXT NOT NULL,
  "petName" TEXT NOT NULL,
  "species" "Species" NOT NULL,
  "description" TEXT NOT NULL,
  "district" TEXT,
  "status" TEXT NOT NULL DEFAULT 'PENDING',
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "AdoptionListing_authorId_fkey" FOREIGN KEY ("authorId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE INDEX "AdoptionListing_status_createdAt_idx" ON "AdoptionListing"("status","createdAt");
CREATE INDEX "AdoptionListing_authorId_idx" ON "AdoptionListing"("authorId");

CREATE TABLE "AdoptionApplication" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "listingId" TEXT NOT NULL,
  "applicantId" TEXT NOT NULL,
  "message" TEXT NOT NULL,
  "status" TEXT NOT NULL DEFAULT 'PENDING',
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "AdoptionApplication_listingId_fkey" FOREIGN KEY ("listingId") REFERENCES "AdoptionListing"("id") ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT "AdoptionApplication_applicantId_fkey" FOREIGN KEY ("applicantId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE UNIQUE INDEX "AdoptionApplication_listingId_applicantId_key" ON "AdoptionApplication"("listingId","applicantId");
CREATE INDEX "AdoptionApplication_applicantId_idx" ON "AdoptionApplication"("applicantId");
