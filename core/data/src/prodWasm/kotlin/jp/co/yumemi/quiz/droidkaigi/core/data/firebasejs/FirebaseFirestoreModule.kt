@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@file:JsModule("firebase/firestore")

package jp.co.yumemi.quiz.droidkaigi.core.data.firebasejs

import kotlin.js.Promise

internal external fun getFirestore(app: FirebaseAppJs): FirestoreJs

/** [path] は `folders/{id}` のようなスラッシュ区切り（偶数セグメント）。 */
internal external fun doc(firestore: FirestoreJs, path: String): DocumentReferenceJs

/** [path] は `folders` や `folders/{id}/rankings` のようなスラッシュ区切り（奇数セグメント）。 */
internal external fun collection(firestore: FirestoreJs, path: String): CollectionReferenceJs

internal external fun getDoc(reference: DocumentReferenceJs): Promise<DocumentSnapshotJs>

internal external fun setDoc(reference: DocumentReferenceJs, data: JsAny): Promise<JsAny?>

internal external fun deleteDoc(reference: DocumentReferenceJs): Promise<JsAny?>

internal external fun getDocs(query: QueryJs): Promise<QuerySnapshotJs>

internal external fun query(query: QueryJs, constraint: QueryConstraintJs): QueryJs

internal external fun query(query: QueryJs, constraint1: QueryConstraintJs, constraint2: QueryConstraintJs): QueryJs

internal external fun where(fieldPath: String, opStr: String, value: JsAny?): QueryConstraintJs

internal external fun orderBy(fieldPath: String, directionStr: String): QueryConstraintJs
