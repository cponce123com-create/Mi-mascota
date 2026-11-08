-- Private one-to-one conversation attached to an approved adoption application
CREATE TABLE "AdoptionMessage" (
  "id" TEXT NOT NULL PRIMARY KEY,
  "applicationId" TEXT NOT NULL,
  "authorId" TEXT NOT NULL,
  "body" TEXT NOT NULL,
  "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT "AdoptionMessage_applicationId_fkey" FOREIGN KEY ("applicationId") REFERENCES "AdoptionApplication"("id") ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT "AdoptionMessage_authorId_fkey" FOREIGN KEY ("authorId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX "AdoptionMessage_applicationId_createdAt_idx" ON "AdoptionMessage"("applicationId","createdAt");
CREATE INDEX "AdoptionMessage_authorId_idx" ON "AdoptionMessage"("authorId");
