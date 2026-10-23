# Preamar

Previsão astronômica local de maré e fase lunar para atividades costeiras
(mergulho, tide pooling, pesca sustentável, observação de vida marinha),
com registro pessoal de saídas vinculado automaticamente às condições
calculadas.

Projeto individual — disciplina **AS65D - Programação Para Dispositivos
Móveis** (turma N15) — UTFPR Campus Cornélio Procópio.

---

## 1. Criar o repositório no GitHub

1. Acesse [github.com/new](https://github.com/new).
2. Nome do repositório: `preamar-android` (ou o nome que preferir).
3. Visibilidade: **Private** (é um projeto individual avaliativo) ou Public, como preferir.
4. **Não** marque "Add a README" nem ".gitignore" — este pacote já traz os dois.
5. Clique em **Create repository** e copie a URL (ex: `git@github.com:seu-usuario/preamar-android.git`).

## 2. Clonar e colocar os arquivos

```bash
git clone git@github.com:seu-usuario/preamar-android.git
cd preamar-android
```

Copie todo o conteúdo desta pasta (`preamar-android/`) para dentro da pasta
que acabou de clonar (ou descompacte o .zip diretamente dentro dela).

```bash
git add .
git commit -m "Entrega Parcial 2: estrutura do projeto, telas iniciais e navegação"
git push
```

## 3. Abrir no Android Studio

1. Abra o Android Studio → **Open** → selecione a pasta `preamar-android`.
2. Aguarde o **Gradle Sync** (primeira vez pode demorar alguns minutos —
   ele baixa as dependências: Material Components, Navigation, Room,
   ConstraintLayout/MotionLayout).
3. Se o Android Studio pedir para gerar o Gradle Wrapper automaticamente,
   aceite (ele substitui a necessidade de incluirmos o `gradlew` binário aqui).
4. Rode em um emulador ou device físico (▶ Run 'app').

> **Dica:** se o Sync reclamar da versão do plugin Android Gradle (AGP) ou
> do Gradle, deixe o próprio Android Studio sugerir o upgrade — ele resolve
> isso automaticamente em "Upgrade Assistant".

## 4. Estrutura do projeto

```
app/src/main/java/com/nunoguerra/preamar/
├── MainActivity.java          # Activity única; hospeda o NavHostFragment
├── ui/
│   ├── home/HomeFragment.java         # Header com parallax (MotionLayout) + dashboard
│   ├── saidas/SaidasFragment.java     # Lista de saídas (RecyclerView)
│   ├── novasaida/NovaSaidaFragment.java  # Formulário de nova saída
│   └── estatisticas/EstatisticasFragment.java
├── data/
│   ├── Saida.java              # Entity Room
│   ├── SaidaDao.java           # Contrato de acesso a dados (CRUD completo na Entrega 3/4)
│   └── AppDatabase.java        # Room database (singleton)
└── util/
    ├── TideCalculator.java     # Motor de maré (stub — implementação na Entrega 3)
    └── MoonPhaseCalculator.java # Motor de fase lunar (stub — implementação na Entrega 3)

app/src/main/res/
├── drawable/ic_turtle_dotted.xml   # Arte stipple (tartaruga) — gerada proceduralmente
├── drawable/ic_ray_dotted.xml      # Arte stipple (arraia) — gerada proceduralmente
├── layout/fragment_home.xml        # MotionLayout: header colapsável + dashboard
├── xml/home_motion_scene.xml       # Define a animação de scroll (parallax)
├── navigation/nav_graph.xml        # Grafo de navegação (4 telas)
└── values/{colors,themes,strings}.xml
```

## 5. Identidade visual

Mesma lógica de design do Elo/Impact Scroll, adaptada pro tema marítimo:

| Elo (web)                     | Preamar (Android)              |
|--------------------------------|----------------------------------|
| Base escura verde-oliva        | Base escura azul profundo (`ocean_deeper` `#072A40`) |
| Texto creme                    | `cream` `#F7F1E3` (mantido)      |
| Acento teal                    | `teal_accent` `#0E7C7B` (mesmo tom, continuidade de marca) |
| Archivo Black (display)        | `sans-serif-black` por padrão — trocar por Archivo Black de verdade em 30s (ver `res/font/LEIA-ME.txt`) |
| GSAP ScrollTrigger              | `MotionLayout` + `OnSwipe` ligado ao `NestedScrollView` (`home_motion_scene.xml`) — header colapsa e a tartaruga pontilhada faz parallax ao rolar |
| Grain/textura                  | Ilustrações stipple (pontilhismo) geradas por script, não clipart |

## 6. O que falta pra cada entrega

- **Entrega 2 (esta)** — ✅ estrutura, telas iniciais, navegação básica.
- **Entrega 3** — implementar `TideCalculator` e `MoonPhaseCalculator` de
  verdade, ligar o CRUD de `SaidaDao` nas telas, integração entre as telas.
- **Entrega 4** — CRUD completo validado, painel de estatísticas com dados
  reais, polimento visual, validações de formulário.

## 7. Observação importante

Este projeto foi montado fora do Android Studio (sem acesso a um SDK/Gradle
real pra compilar), então o Gradle Sync pode pedir pequenos ajustes de
versão de plugin na primeira abertura — é normal e o próprio Android Studio
resolve. Se algum XML do MotionLayout der erro de atributo, o Preview do
Android Studio aponta a linha exata; é rápido de ajustar.

## Estado atual: Entrega Parcial 3 — Desenvolvimento Intermediário

**Funcionalidades do sistema**
- Motor de fase lunar (`MoonPhaseCalculator`): ciclo sinódico médio, idade, iluminação e 8 fases.
- Motor de maré (`TideCalculator`): modelo semidiurno (~12h25) com sizígia/quadratura,
  estado da maré (alta, baixa, enchendo, vazando) e lista de preamares/baixa-mares.
- Recomendação (`Recomendador`): nota 0–100 por atividade a cada 30 min e melhores janelas das próximas 48 h.
- Registro de saídas (Room): ao salvar, as condições de lua/maré são calculadas e gravadas automaticamente.
- Persistência 100% local (Room, banco `preamar.db`, versão 2).

**Integração entre telas**
- Home → Nova saída (FAB) → salva no Room → Saídas (lista) e Home (última saída) atualizam via LiveData.
- Saídas/Home → Detalhe da saída (argumento `saidaId`) → excluir → volta para a lista.
- Barra inferior some nas telas secundárias (nova saída e detalhe).

**Interface**
- Cabeçalho colapsável (CollapsingToolbarLayout) com tartaruga pontilhada em parallax.
- Gráfico de maré e fase da lua desenhados em Canvas (`TideChartView`, `MoonPhaseView`).
- Chips de atividade, cards com entrada animada, textura de pontos no fundo.

## Limitações assumidas (honestidade técnica)
- A maré é uma **estimativa astronômica aproximada** (sem batimetria nem dados de estação); não
  substitui a tábua de marés da Marinha (DHN). A longitude vem do fuso do aparelho.
- A fase lunar usa o ciclo médio (erro de algumas horas).
- As regras de recomendação são heurísticas por atividade.

## Próximas entregas
- **Entrega 4:** edição de saídas, validações completas, painel de estatísticas (fase lunar x saídas), polimento visual.
- **Final:** documentação, vídeo (até 5 min).

