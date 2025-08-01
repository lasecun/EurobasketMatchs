# 🛡️ Reglas de Protección de Ramas

## ⚠️ Estado Actual: Sin Protección
**La rama `main` NO tiene reglas de protección configuradas actualmente.**

## 🔧 Configuración Manual (Recomendado)

### Pasos para configurar en GitHub Web:

1. **Ir a la configuración del repositorio:**
   - Ve a: https://github.com/lasecun/EurobasketMatchs/settings/branches
   - O navega: Repositorio → Settings → Branches

2. **Crear nueva regla:**
   - Clic en "Add rule" o "Add branch protection rule"
   - En "Branch name pattern" escribe: `main`

3. **Configurar las siguientes opciones:**

### 🔒 Reglas de Protección para `main`

#### ✅ Configuración OBLIGATORIA:
- ☑️ **Require a pull request before merging**
  - ☑️ Require approvals: **1** mínimo
  - ☑️ Dismiss stale PR approvals when new commits are pushed
  - ☑️ Require review from code owners (opcional)

#### ✅ Configuración de Status Checks:
- ☑️ **Require status checks to pass before merging**
- ☑️ **Require branches to be up to date before merging**
- Status checks requeridos (seleccionar cuando aparezcan):
  - `Pull Request Validation / 🔍 PR Validation`
  - `� CI Pipeline - Build & Test / 🏗️ Build & Test`

#### ✅ Configuración de Restricciones:
- ☑️ **Restrict pushes that create files** (solo administradores)
- ☑️ **Do not allow bypassing the above settings** (incluir administradores)

#### ✅ Configuración Adicional:
- ☑️ **Allow force pushes**: ❌ DESHABILITADO
- ☑️ **Allow deletions**: ❌ DESHABILITADO

## 🔧 Comandos para configurar vía GitHub CLI

```bash
# Habilitar protección de rama main
gh api repos/:owner/:repo/branches/main/protection \
  --method PUT \
  --field required_status_checks='{"strict":true,"checks":[{"context":"🔍 PR Validation"},{"context":"🏗️ Build & Test"},{"context":"🔍 Code Quality Analysis"}]}' \
  --field enforce_admins=true \
  --field required_pull_request_reviews='{"required_approving_review_count":1,"dismiss_stale_reviews":true}' \
  --field restrictions=null \
  --field allow_force_pushes=false \
  --field allow_deletions=false

# Verificar configuración
gh api repos/:owner/:repo/branches/main/protection
```

## 📋 Verificación de la configuración

**⚠️ IMPORTANTE: Ejecuta estas pruebas después de aplicar las reglas**

### Prueba 1: Intentar push directo a main
```bash
# Esto debe FALLAR si las reglas están configuradas
git checkout main
git push origin main
# Resultado esperado: ERROR - protected branch
```

### Prueba 2: Verificar que los PRs requieren aprobación
1. Crear un PR hacia `main`
2. Verificar que aparece: "Review required"
3. Verificar que NO se puede hacer merge sin aprobación

### Prueba 3: Verificar status checks
1. En un PR hacia `main`
2. Verificar que aparecen los checks obligatorios
3. Confirmar que no se puede hacer merge si fallan

## ✅ Lista de Verificación Final

Una vez configurado, marca las siguientes verificaciones:

- [ ] ❌ No se puede hacer `git push` directo a `main`
- [ ] ❌ No se puede borrar la rama `main`  
- [ ] ✅ Todos los cambios requieren PR
- [ ] ✅ Los PRs requieren mínimo 1 aprobación
- [ ] ✅ Los checks de CI deben pasar antes del merge
- [ ] ❌ No se permiten force pushes a `main`

## 🚨 Emergencias

En caso de emergencia, solo los administradores del repositorio pueden:
- Desactivar temporalmente las reglas
- Hacer push directo (si se permite en la configuración)
- Forzar merge sin aprobaciones

## 📖 Referencias

- [GitHub Branch Protection Rules](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/defining-the-mergeability-of-pull-requests/about-protected-branches)
- [GitHub CLI Branch Protection](https://cli.github.com/manual/gh_api)
