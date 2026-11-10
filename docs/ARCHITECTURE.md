# المعمارية المقترحة

## الوحدات
- `android/app` نقطة الدخول، التنقل، DI.
- `android/core/designsystem` RTL والألوان وإتاحة الوصول.
- `android/core/database` تخزين Room لبيانات النعي والمحتوى المحمّل.
- `android/core/network` العميل والوثوق بالشهادات والمهلات.
- `android/feature/obituary` تحرير وتصدير محلي.
- `android/feature/education` المحتوى المراجع.
- `android/feature/map` البحث الجغرافي واختيار مكان يدوي.
- `android/feature/assistance` الطلبات وحالاتها.
- `android/feature/providers` ملفات الجهات والمتطوعين.
- `backend` Laravel API مع سياسات صلاحية ومراقبة وصول.
- `admin` لوحة مراجعة تحقق وشكاوى وتوثيق.

## نماذج البيانات المقترحة
| الكيان | حقول نموذجية |
| --- | --- |
| users | id, role, status, created_at |
| organizations | id, type, legal_name, verified_at, verification_status |
| provider_profiles | id, user_id, skill_type, service_area, availability_status |
| service_locations | id, organization_id, geom(Point,4326), address_area, last_verified_at |
| services | id, location_id, service_type, price_category, eligibility_notes, status |
| service_requests | id, requester_id, service_type, area, state, expires_at |
| request_assignments | id, request_id, provider_id, state, assigned_at |
| education_content | id, subject, source_refs, review_state, reviewer_id, version |
| obituaries | id, owner_id, publish_state, consent_record_id, expiry_at |
| memorial_pages | id, obituary_id, visibility, owner_id |
| reports | id, subject_type, subject_id, reason, state |
| audit_logs | id, actor_id, action, subject, timestamp |

## الوصول والخصوصية
- الصلاحيات الافتراضية منع، والسياسات على مستوى الكائن لا الدور وحده.
- النعي محلي افتراضيًا؛ الرفع منفصل باختيار صريح.
- لا يُعرض رقم هاتف خاص أو موقع منزل المتطوع في نتائج البحث العامة.
- لا تحتفظ بخط الطول والعرض الدقيق للمستخدم بعد البحث إلا لضرورة موثقة وموافقة محددة.
- سجلات التدقيق لا تحتوي مستندات هوية أو أرقام هواتف كاملة.
- تعريف حدود الاحتفاظ والحذف قبل اختبار الخدمة الحقيقية.
- واجهة API لا تعتمد على التحقق من العميل فقط؛ تحقق من الصلاحيات والمدخلات والحدود على الخادم.
- نصوص المستخدم ومحتوى الشكاوى غير موثوق؛ طبّق ترشيح XSS وتحديد معدلات الطلب.

## مثال API مستقبلي (لم يُنفذ)
`GET /api/v1/services?lat=...&lng=...&radius_km=10&type=washing`
`POST /api/v1/providers/applications`
`POST /api/v1/service-requests`
`GET /api/v1/education?subject=ghusl`

تحديد التوافق النهائي للإصدارات والتراخيص ومكتبة الخرائط أثناء التنفيذ، لا على أساس التخمين.
