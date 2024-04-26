# Prep

## What we know so far
 - Scala implicits, TC (type class) pattern, extension methods
 - Cats type classes

<div style="width:100%">
<span style="width:auto; float:left">

## Major type classes
 - `Semigroup`
   - Methods: `combine`
   - Syntax: `|+|`
 - `Monoid` extends `Semigroup`
   - Methods: `empty`
 - `Functor`
   - Methods: `map`
 - `Monad` extends `Functor`
   - Methods: `pure`, `flatMap`
   - Syntax: `for-comprehension`, `>>`, `*>`, `<<`, `<*`

</span>
<span style="width:60%; text-align:center">

[![Class Diagram](https://mermaid.ink/img/pako:eNo1j8EKgzAQRH8l7MmC_YEceijFm3hITyWXJVk1YLKSJpQi_nuj1j3tzDwYZgHDlkBCP_HHjBiTuD91EOVUpci7IXKeL4fTVi0HdvaU4nq9CXWIpmpyMInjP-w2Fk-029EGavAUPTpbGpct0pBG8qRBltdSj3lKGnRYC4o5sfoGAzLFTDXk2WKih8MhogfZ4_QuLllXWttjxT6mhhnDi_lk1h_0Okef?type=svg)](https://mermaid.live/edit#pako:eNo1j8EKgzAQRH8l7MmC_YEceijFm3hITyWXJVk1YLKSJpQi_nuj1j3tzDwYZgHDlkBCP_HHjBiTuD91EOVUpci7IXKeL4fTVi0HdvaU4nq9CXWIpmpyMInjP-w2Fk-029EGavAUPTpbGpct0pBG8qRBltdSj3lKGnRYC4o5sfoGAzLFTDXk2WKih8MhogfZ4_QuLllXWttjxT6mhhnDi_lk1h_0Okef)

</span>
</div>
