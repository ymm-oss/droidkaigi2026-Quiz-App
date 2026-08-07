#!/usr/bin/env node
/**
 * Uploads a staff Desktop DMG to Firebase Storage and writes staffAppRelease/latest.
 *
 * Env:
 *   VERSION, VERSION_CODE, STORAGE_PATH, RELEASE_NOTES, OVERWRITE
 *   FIREBASE_SERVICE_ACCOUNT (JSON string) or GOOGLE_APPLICATION_CREDENTIALS
 *   GOOGLE_SERVICES_JSON (optional; reads project_info.storage_bucket)
 *   FIREBASE_STORAGE_BUCKET (optional override)
 *
 * Args:
 *   [0] path to local .dmg
 */
const fs = require("fs");
const path = require("path");
const crypto = require("crypto");
const admin = require("firebase-admin");

async function main() {
  const dmgPath = process.argv[2];
  if (!dmgPath || !fs.existsSync(dmgPath)) {
    console.error("Usage: node publish-staff-release.mjs <dmg-path>");
    process.exit(1);
  }

  const version = requiredEnv("VERSION");
  const versionCode = Number(requiredEnv("VERSION_CODE"));
  const storagePath = requiredEnv("STORAGE_PATH");
  const releaseNotes = process.env.RELEASE_NOTES || "";
  const overwrite = String(process.env.OVERWRITE || "false").toLowerCase() === "true";

  const credential = resolveCredential();
  const projectId =
    credential.project_id ||
    process.env.GCLOUD_PROJECT ||
    process.env.GOOGLE_CLOUD_PROJECT ||
    "droidkaigi26";
  const storageBucket = resolveStorageBucket(projectId);

  admin.initializeApp({
    credential: admin.credential.cert(credential),
    storageBucket,
  });

  const bucket = admin.storage().bucket();
  const file = bucket.file(storagePath);
  const [exists] = await file.exists();
  if (exists && !overwrite) {
    console.error(
      `Storage object already exists: gs://${bucket.name}/${storagePath}. Pass overwrite=true to replace.`,
    );
    process.exit(1);
  }

  const sha256 = sha256File(dmgPath);
  console.log(`Uploading ${dmgPath} -> gs://${bucket.name}/${storagePath}`);
  await bucket.upload(dmgPath, {
    destination: storagePath,
    metadata: {
      contentType: "application/x-apple-diskimage",
      metadata: {
        version,
        versionCode: String(versionCode),
        sha256,
      },
    },
  });

  const publishedAtEpochMillis = Date.now();
  const doc = {
    version,
    versionCode,
    storagePath,
    sha256,
    releaseNotes,
    publishedAtEpochMillis,
  };
  console.log("Writing Firestore staffAppRelease/latest", doc);
  await admin.firestore().doc("staffAppRelease/latest").set(doc, { merge: false });
  console.log("Published staff Desktop release", version);
}

function requiredEnv(name) {
  const value = process.env[name];
  if (!value) {
    console.error(`Missing required env: ${name}`);
    process.exit(1);
  }
  return value;
}

function resolveCredential() {
  if (process.env.FIREBASE_SERVICE_ACCOUNT) {
    return JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
  }
  const credPath = process.env.GOOGLE_APPLICATION_CREDENTIALS;
  if (credPath && fs.existsSync(credPath)) {
    return JSON.parse(fs.readFileSync(credPath, "utf8"));
  }
  console.error("FIREBASE_SERVICE_ACCOUNT or GOOGLE_APPLICATION_CREDENTIALS is required");
  process.exit(1);
}

function resolveStorageBucket(projectId) {
  if (process.env.FIREBASE_STORAGE_BUCKET) {
    return process.env.FIREBASE_STORAGE_BUCKET;
  }
  const gsJson = process.env.GOOGLE_SERVICES_JSON;
  if (gsJson) {
    try {
      const parsed = JSON.parse(gsJson);
      const bucket = parsed.project_info?.storage_bucket;
      if (bucket) return bucket;
    } catch (error) {
      console.error("Failed to parse GOOGLE_SERVICES_JSON for storage_bucket", error);
      process.exit(1);
    }
  }
  return `${projectId}.appspot.com`;
}

function sha256File(filePath) {
  const hash = crypto.createHash("sha256");
  hash.update(fs.readFileSync(filePath));
  return hash.digest("hex");
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
