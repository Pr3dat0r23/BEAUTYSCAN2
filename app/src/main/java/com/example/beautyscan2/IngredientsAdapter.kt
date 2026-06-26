package com.example.beautyscan2

import android.app.AlertDialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class IngredientsAdapter(private val ingredients: List<String>) :
    RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    companion object {
        // CZARNA LISTA (czerwony) - szkodliwe, mikroplastiki, silne konserwanty, pochodne ropy naftowej
        // Używamy mapOf (Klucz -> Wartość) do przechowywania nazwy i opisu.
        val badIngredients = mapOf(
            "paraben" to "Konserwanty, które mogą przenikać przez skórę do krwiobiegu. Podejrzewa się je o zaburzanie gospodarki hormonalnej.",
            "sls" to "Sodium Lauryl Sulfate. Bardzo agresywny detergent myjący. Może silnie wysuszać, podrażniać skórę i niszczyć jej naturalną barierę hydrolipidową.",
            "sles" to "Sodium Laureth Sulfate. Pochodna SLS. Choć łagodniejsza, w procesie produkcji może być zanieczyszczona szkodliwym dioksanem.",
            "peg" to "Glikole polietylenowe. Sprawiają, że naskórek staje się bardziej przepuszczalny, przez co do organizmu mogą wnikać toksyny i zanieczyszczenia z otoczenia.",
            "dimethicone" to "Silikon, który tworzy na skórze nieprzepuszczalną warstwę (film). Może zatykać pory, nasilać trądzik i utrudniać skórze oddychanie.",
            "bht" to "Syntetyczny przeciwutleniacz. Uznawany za potencjalnie toksyczny alergen. Przy długim stosowaniu może negatywnie wpływać na narządy wewnętrzne.",
            "edta" to "Składnik wiążący jony metali. Sam w sobie nie jest silnie toksyczny, ale ułatwia przenikanie innych, szkodliwych substancji z kosmetyku głęboko w skórę.",
            "triclosan" to "Silny środek antybakteryjny. Może zaburzać naturalną florę bakteryjną skóry, wpływać na gospodarkę hormonalną i przyczyniać się do powstawania lekoopornych bakterii.",
            "formaldehyde" to "Formaldehyd (i jego donory). Silnie toksyczny gaz używany jako konserwant. Częsty alergen, uznawany za substancję o potencjale rakotwórczym.",
            "phthalate" to "Ftalany. Związki chemiczne często ukryte w kompozycjach zapachowych. Udowodniono, że mogą negatywnie wpływać na układ rozrodczy i hormonalny.",
            "petrolatum" to "Wazelina (pochodna ropy naftowej). Tworzy na skórze tzw. okluzję ciągłą, która może prowadzić do powstawania zaskórników i trądziku.",
            "mineral oil" to "Olej mineralny (parafina ciekła). Produkt destylacji ropy naftowej. Nie wnosi wartości odżywczych, tworzy nieprzepuszczalny film zatykający pory.",
            "paraffin" to "Parafina. Podobnie jak olej mineralny i wazelina, to składnik ropopochodny, który blokuje naturalne procesy wydzielnicze skóry.",
            "methylisothiazolinone" to "Bardzo silny konserwant (często oznaczany jako MI). Został zidentyfikowany jako przyczyna wielu silnych kontaktowych reakcji alergicznych skóry.",
            "cyclopentasiloxane" to "Lotny silikon (D5). Istnieją obawy dotyczące jego negatywnego wpływu na środowisko (bioakumulacja) oraz potencjalnego działania drażniącego u ludzi.",
            "toluene" to "Toluen. Toksyczny rozpuszczalnik spotykany czasem w lakierach do paznokci. Wdychanie jego oparów jest szkodliwe dla układu nerwowego.",
            "polyacrylamide" to "Polimer stabilizujący konsystencję. Może być zanieczyszczony akrylamidem, który jest substancją toksyczną i potencjalnie rakotwórczą.",
            "bha" to "Butylohydroksyanizol. Konserwant o działaniu antyoksydacyjnym. Międzynarodowa Agencja Badań nad Rakiem uznaje go za prawdopodobnie rakotwórczy dla ludzi.",
            "coal tar" to "Smoła węglowa. Składnik używany niegdyś powszechnie w kosmetykach przeciwłupieżowych. Uznawany za substancję o potencjale rakotwórczym.",
            "p-phenylenediamine" to "PPD (tzw. sztuczna henna). Barwnik stosowany w farbach do włosów. Bardzo silny alergen, który może wywołać ciężkie, bolesne reakcje skórne."
        )

        // ŻÓŁTA LISTA (pomarańczowy) - potencjalnie drażniące, popularne alergeny zapachowe, wysuszające alkohole
        val warningIngredients = mapOf(
            "parfum" to "Sztuczna kompozycja zapachowa. Często to zbiór nieujawnionych chemikaliów. Należy do najczęstszych przyczyn alergii kosmetycznych.",
            "fragrance" to "Odpowiednik 'Parfum'. Sztuczny zapach mogący ukrywać w swoim składzie potencjalnie drażniące substancje i alergeny.",
            "phenoxyethanol" to "Konserwant będący alternatywą dla parabenów. W wysokich stężeniach może drażnić skórę i oczy. Bezpieczny w ilościach poniżej 1%.",
            "alcohol denat" to "Alkohol denaturowany. Bardzo szybko odparowuje, ale silnie wysusza skórę, niszczy barierę lipidową i może powodować zaczerwienienia.",
            "propylene glycol" to "Glikol propylenowy. Promotor przejścia – ułatwia przenikanie substancji w głąb skóry. W wyższych stężeniach może działać drażniąco na cerę wrażliwą.",
            "limonene" to "Składnik kompozycji zapachowych (cytrusowy). Pod wpływem powietrza utlenia się, tworząc związki, które mogą silnie uczulać skórę.",
            "linalool" to "Składnik dodawany jako substancja zapachowa. Jest bardzo podatny na utlenianie, po którym staje się mocnym alergenem kontaktowym.",
            "geraniol" to "Substancja o zapachu przypominającym różę. Znajduje się na liście potencjalnych alergenów kontaktowych – ostrożnie przy cerze ultrawrażliwej.",
            "citronellol" to "Alergen zapachowy (kwiatowo-cytrusowy). Często stosowany, jednak u osób ze skłonnością do atopii może wywołać podrażnienia i pieczenie.",
            "citral" to "Substancja zapachowa o aromacie cytryny. Podobnie jak inne naturalne alergeny zapachowe, wymaga ostrożności przy skórach skłonnych do uczuleń.",
            "eugenol" to "Związek o mocnym, korzennym zapachu (np. goździków). Działa drażniąco na skórę, jest znanym alergenem wymagającym ostrożności.",
            "cocamidopropyl betaine" to "Detergent (CAPB). Sam w sobie łagodny, ale często bywa zanieczyszczony produktami ubocznymi produkcji (amidoaminy), co powoduje alergie.",
            "talc" to "Talk. Substancja wchłaniająca wilgoć. Kontrowersje budzi fakt, że w przeszłości złoża talku bywały zanieczyszczone toksycznym azbestem.",
            "benzyl alcohol" to "Alkohol benzylowy. Używany jako konserwant i rozpuszczalnik zapachów. W większych ilościach może wywoływać pieczenie wrażliwej skóry.",
            "aluminum" to "Sole aluminium (np. w antyperspirantach). Blokują gruczoły potowe. Często budzą kontrowersje, dlatego wiele osób woli ich unikać."
        )
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvIngredientName: TextView = view.findViewById(R.id.tvIngredientName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position].trim()
        holder.tvIngredientName.text = ingredient

        val lowerCaseIngredient = ingredient.lowercase()

        // Szukamy, czy nasz składnik z bazy zawiera w sobie któryś z kluczy (słów) z naszych map
        val badMatch = badIngredients.entries.find { lowerCaseIngredient.contains(it.key) }
        val warningMatch = warningIngredients.entries.find { lowerCaseIngredient.contains(it.key) }

        // Ustalamy stany boolowskie do kolorowania
        val isBad = badMatch != null
        val isWarning = warningMatch != null

        // Wyciągamy gotowy tekst opisu (z czerwonej mapy, a jeśli null to z żółtej mapy)
        val descriptionText = badMatch?.value ?: warningMatch?.value

        // Przypisywanie kolorów na podstawie list
        when {
            isBad -> {
                // Czerwony - silnie szkodliwy
                holder.tvIngredientName.setTextColor(Color.parseColor("#D32F2F"))
            }
            isWarning -> {
                // Pomarańczowy - ostrzeżenie
                holder.tvIngredientName.setTextColor(Color.parseColor("#F57F17"))
            }
            else -> {
                // Zielony - bezpieczny / neutralny
                holder.tvIngredientName.setTextColor(Color.parseColor("#388E3C"))
            }
        }

        // DODANO: Nasłuchiwanie kliknięcia na dany składnik
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context

            if (descriptionText != null) {
                // Jeśli znaleźliśmy opis (czerwony lub pomarańczowy), pokazujemy okienko AlertDialog
                AlertDialog.Builder(context)
                    .setTitle("Analiza składnika")
                    .setMessage("Wybrany składnik:\n$ingredient\n\nDlaczego na niego uważać?\n$descriptionText")
                    .setPositiveButton("Zamknij", null)
                    .show()
            } else {
                // Jeśli to składnik zielony (brak opisu), pokazujemy tylko mały dymek (Toast)
                Toast.makeText(
                    context,
                    "Składnik uznawany za bezpieczny.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount() = ingredients.size
}